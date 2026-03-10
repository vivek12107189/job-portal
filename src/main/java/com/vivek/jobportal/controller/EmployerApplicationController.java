package com.vivek.jobportal.controller;

import com.vivek.jobportal.dto.EmployerApplicationResponse;
import com.vivek.jobportal.dto.UpdateApplicationStatusRequest;
import com.vivek.jobportal.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer/applications")
public class EmployerApplicationController {

    private final ApplicationService applicationService;

    public EmployerApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<EmployerApplicationResponse> myJobApplications(
            @AuthenticationPrincipal String email
    ) {
        return applicationService.applicationsForMyJobs(email);
    }

    @PatchMapping("/{applicationId}/status")
    public EmployerApplicationResponse updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody @Valid UpdateApplicationStatusRequest request,
            @AuthenticationPrincipal String email
    ) {
        return applicationService.updateApplicationStatus(applicationId, request, email);
    }
}
