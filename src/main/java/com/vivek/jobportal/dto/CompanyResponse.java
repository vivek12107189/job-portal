package com.vivek.jobportal.dto;

import com.vivek.jobportal.entity.Company;

public record CompanyResponse(Long id, String name,String description,String location) {


}
