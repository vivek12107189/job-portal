package com.vivek.jobportal.dto;

import com.vivek.jobportal.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(
        @NotNull ApplicationStatus status
) {
}
