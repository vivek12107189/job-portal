package com.vivek.jobportal.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.rate-limit")
public record AuthRateLimitProperties(
        @Valid Limit loginByIp,
        @Valid Limit loginByEmail,
        @Valid Limit refreshByIp
) {
    public record Limit(
            @Min(1) int maxAttempts,
            @Min(1) long windowSeconds
    ) {
    }
}
