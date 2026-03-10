package com.vivek.jobportal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateJobRequest {

    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 10000)
    private String description;

    @Size(max = 255)
    private String location;

    @DecimalMin(value = "0.0", inclusive = true)
    private Double salary;
}
