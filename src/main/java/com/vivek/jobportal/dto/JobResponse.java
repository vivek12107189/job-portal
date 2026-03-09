package com.vivek.jobportal.dto;

public record JobResponse(
        Long id,
        String title,
        String description,
        String location,
        Double salary,
        Long companyId
) {
}
