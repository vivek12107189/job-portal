package com.vivek.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateJobRequest {

    @NotBlank
    private String title;

    private String description;

    private String location;

    private Double salary;

    @NotNull
    private Long companyId;
}
