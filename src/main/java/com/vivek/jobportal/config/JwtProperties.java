package com.vivek.jobportal.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Min(60000) long expirationMs,
        @Min(60000) long refreshExpirationMs
) {
    public JwtProperties {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        if ("replace-with-at-least-32-char-secret".equals(secret)) {
            throw new IllegalArgumentException("JWT secret must not use the placeholder value");
        }
    }
}
