package com.vivek.jobportal.controller;

import com.vivek.jobportal.dto.ApplicationResponse;
import com.vivek.jobportal.service.ApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/apply/{jobId}")
    public ApplicationResponse apply(
            @PathVariable Long jobId,
            @AuthenticationPrincipal String email
    ) {
        return applicationService.apply(jobId, email);
    }

    @GetMapping("/my")
    public List<ApplicationResponse> myApplications(
            @AuthenticationPrincipal String email
    ) {
        return applicationService.myApplications(email);
    }
}
