package com.vivek.jobportal.service;

import com.vivek.jobportal.config.AuthRateLimitProperties;
import com.vivek.jobportal.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthRateLimitServiceTest {

    @Test
    void loginIsBlockedAfterConfiguredFailedAttempts() {
        AuthRateLimitService service = new AuthRateLimitService(properties(), new MutableClock());
        MockHttpServletRequest request = request("10.0.0.1");

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> service.checkLoginAllowed("user@test.com", request));
            service.recordLoginFailure("user@test.com", request);
        }

        assertThrows(TooManyRequestsException.class,
                () -> service.checkLoginAllowed("user@test.com", request));
    }

    @Test
    void successfulLoginResetsFailureCounters() {
        AuthRateLimitService service = new AuthRateLimitService(properties(), new MutableClock());
        MockHttpServletRequest request = request("10.0.0.2");

        service.recordLoginFailure("user@test.com", request);
        service.recordLoginFailure("user@test.com", request);
        service.resetLoginFailures("user@test.com", request);

        assertDoesNotThrow(() -> service.checkLoginAllowed("user@test.com", request));
    }

    @Test
    void refreshIsBlockedAfterConfiguredAttempts() {
        AuthRateLimitService service = new AuthRateLimitService(properties(), new MutableClock());
        MockHttpServletRequest request = request("10.0.0.3");

        for (int i = 0; i < 20; i++) {
            assertDoesNotThrow(() -> service.consumeRefreshAttempt(request));
        }

        assertThrows(TooManyRequestsException.class, () -> service.consumeRefreshAttempt(request));
    }

    private AuthRateLimitProperties properties() {
        return new AuthRateLimitProperties(
                new AuthRateLimitProperties.Limit(10, 900),
                new AuthRateLimitProperties.Limit(5, 900),
                new AuthRateLimitProperties.Limit(20, 300)
        );
    }

    private MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    private static final class MutableClock extends Clock {
        private final Instant instant = Instant.parse("2026-03-10T10:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
