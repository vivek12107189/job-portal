package com.vivek.jobportal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.NumberFormat;

public record JobSearchRequest(
        String keyword,
        String location,
        Long companyId,
        @NumberFormat Double minSalary,
        @NumberFormat Double maxSalary,
        String sortBy,
        String direction,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {
    public int resolvedPage() {
        return page == null ? 0 : page;
    }

    public int resolvedSize() {
        return size == null ? 10 : size;
    }

    public String resolvedSortBy() {
        return sortBy == null ? "createdAt" : sortBy;
    }

    public String resolvedDirection() {
        return direction == null ? "desc" : direction;
    }
}
