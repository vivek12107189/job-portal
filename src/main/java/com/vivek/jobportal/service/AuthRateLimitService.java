package com.vivek.jobportal.service;

import com.vivek.jobportal.config.AuthRateLimitProperties;
import com.vivek.jobportal.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthRateLimitService {

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final AuthRateLimitProperties properties;
    private final Clock clock;
    private final AtomicInteger cleanupTicker = new AtomicInteger();

    public AuthRateLimitService(AuthRateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public void checkLoginAllowed(String email, HttpServletRequest request) {
        ensureAllowed(loginIpKey(request), properties.loginByIp(), "Too many login attempts from this IP");
        ensureAllowed(loginEmailKey(email), properties.loginByEmail(), "Too many login attempts for this account");
    }

    public void recordLoginFailure(String email, HttpServletRequest request) {
        increment(loginIpKey(request), properties.loginByIp());
        increment(loginEmailKey(email), properties.loginByEmail());
    }

    public void resetLoginFailures(String email, HttpServletRequest request) {
        counters.remove(loginIpKey(request));
        counters.remove(loginEmailKey(email));
    }

    public void consumeRefreshAttempt(HttpServletRequest request) {
        consume(refreshIpKey(request), properties.refreshByIp(), "Too many token refresh attempts from this IP");
    }

    private void ensureAllowed(String key, AuthRateLimitProperties.Limit limit, String message) {
        maybeCleanup();
        Counter counter = counters.get(key);
        long now = nowEpochSeconds();
        if (counter == null || counter.windowStartEpochSeconds() + limit.windowSeconds() <= now) {
            return;
        }
        if (counter.count() >= limit.maxAttempts()) {
            throw new TooManyRequestsException(message, retryAfterSeconds(counter, limit, now));
        }
    }

    private void increment(String key, AuthRateLimitProperties.Limit limit) {
        maybeCleanup();
        counters.compute(key, (ignored, current) -> {
            long now = nowEpochSeconds();
            if (current == null || current.windowStartEpochSeconds() + limit.windowSeconds() <= now) {
                return new Counter(now, 1);
            }
            return new Counter(current.windowStartEpochSeconds(), current.count() + 1);
        });
    }

    private void consume(String key, AuthRateLimitProperties.Limit limit, String message) {
        maybeCleanup();
        long now = nowEpochSeconds();
        AtomicLong retryAfterSeconds = new AtomicLong();

        counters.compute(key, (ignored, current) -> {
            if (current == null || current.windowStartEpochSeconds() + limit.windowSeconds() <= now) {
                return new Counter(now, 1);
            }
            if (current.count() >= limit.maxAttempts()) {
                retryAfterSeconds.set(retryAfterSeconds(current, limit, now));
                return current;
            }
            return new Counter(current.windowStartEpochSeconds(), current.count() + 1);
        });

        if (retryAfterSeconds.get() > 0) {
            throw new TooManyRequestsException(message, retryAfterSeconds.get());
        }
    }

    private long retryAfterSeconds(Counter counter, AuthRateLimitProperties.Limit limit, long now) {
        long retryAfter = (counter.windowStartEpochSeconds() + limit.windowSeconds()) - now;
        return Math.max(retryAfter, 1);
    }

    private String loginIpKey(HttpServletRequest request) {
        return "login:ip:" + clientIp(request);
    }

    private String loginEmailKey(String email) {
        return "login:email:" + normalizeEmail(email);
    }

    private String refreshIpKey(HttpServletRequest request) {
        return "refresh:ip:" + clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private long nowEpochSeconds() {
        return Instant.now(clock).getEpochSecond();
    }

    private void maybeCleanup() {
        if (cleanupTicker.incrementAndGet() % 100 != 0) {
            return;
        }

        long now = nowEpochSeconds();
        counters.entrySet().removeIf(entry -> isExpired(entry.getKey(), entry.getValue(), now));
    }

    private boolean isExpired(String key, Counter counter, long now) {
        AuthRateLimitProperties.Limit limit = key.startsWith("refresh:")
                ? properties.refreshByIp()
                : key.startsWith("login:email:")
                ? properties.loginByEmail()
                : properties.loginByIp();
        return counter.windowStartEpochSeconds() + limit.windowSeconds() <= now;
    }

    private record Counter(long windowStartEpochSeconds, int count) {
    }
}
