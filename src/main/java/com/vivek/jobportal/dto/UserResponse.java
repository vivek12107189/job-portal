package com.vivek.jobportal.dto;

public record UserResponse(Long id,
                           String name,
                           String role,
                           String email
) {

}
