package com.vivek.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class CreateCompanyRequest {

    @NotBlank
    private String name;

    private String description;

    private String location;
}
