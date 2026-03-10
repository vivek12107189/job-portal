package com.vivek.jobportal.controller;

import com.vivek.jobportal.dto.CreateJobRequest;
import com.vivek.jobportal.dto.JobSearchRequest;
import com.vivek.jobportal.dto.JobResponse;
import com.vivek.jobportal.dto.PageResponse;
import com.vivek.jobportal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(
            @RequestBody @Valid CreateJobRequest request,
            @AuthenticationPrincipal String email
    ) {
        return jobService.createJob(request, email);
    }

    @GetMapping
    public PageResponse<JobResponse> allJobs(@Valid JobSearchRequest request) {
        return jobService.searchJobs(request);
    }

    @GetMapping("/{id}")
    public JobResponse jobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }
}
