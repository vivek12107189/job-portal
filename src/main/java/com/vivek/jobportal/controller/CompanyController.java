package com.vivek.jobportal.controller;


import com.vivek.jobportal.dto.CompanyResponse;
import com.vivek.jobportal.dto.CreateCompanyRequest;
import com.vivek.jobportal.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public CompanyResponse create(
            @RequestBody @Valid CreateCompanyRequest request,
            @AuthenticationPrincipal String email
            ){
        return companyService.createCompany(request,email);
    }

    @GetMapping("/my")
    public List<CompanyResponse> myCompanies(@AuthenticationPrincipal String email){
        return companyService.myCompanies(email);
    }
}
