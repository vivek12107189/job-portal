package com.vivek.jobportal.dto;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String status,
        LocalDateTime appliedAt
) {}
