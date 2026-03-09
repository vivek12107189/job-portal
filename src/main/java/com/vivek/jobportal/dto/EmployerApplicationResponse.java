package com.vivek.jobportal.dto;

import java.time.LocalDateTime;

public record EmployerApplicationResponse(
        Long applicationId,
        Long jobId,
        String jobTitle,
        String applicantName,
        String applicantEmail,
        String status,
        LocalDateTime appliedAt
) {}
