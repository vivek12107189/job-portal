package com.vivek.jobportal.controller;

import com.vivek.jobportal.dto.JobResponse;
import com.vivek.jobportal.dto.PageResponse;
import com.vivek.jobportal.dto.UpdateJobRequest;
import com.vivek.jobportal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employer/jobs")
public class EmployerJobController {

    private final JobService jobService;

    public EmployerJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public PageResponse<JobResponse> myJobs(@AuthenticationPrincipal String email) {
        return jobService.getEmployerJobs(email);
    }

    @PatchMapping("/{id}")
    public JobResponse updateJob(@PathVariable Long id,
                                 @RequestBody @Valid UpdateJobRequest request,
                                 @AuthenticationPrincipal String email) {
        return jobService.updateJob(id, request, email);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id,
                          @AuthenticationPrincipal String email) {
        jobService.deleteJob(id, email);
    }
}
