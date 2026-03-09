package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.CreateJobRequest;
import com.vivek.jobportal.dto.JobResponse;
import com.vivek.jobportal.entity.Company;
import com.vivek.jobportal.entity.Job;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.repository.CompanyRepository;
import com.vivek.jobportal.repository.JobRepository;
import com.vivek.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository,
                      CompanyRepository companyRepository,
                      UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public JobResponse createJob(CreateJobRequest request, String email) {

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BadRequestException("Company not found"));

        // 🔐 Ownership check
        if (!company.getCreatedBy().getId().equals(employer.getId())) {
            throw new BadRequestException("You are not allowed to post jobs for this company");
        }

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .salary(request.getSalary())
                .company(company)
                .build();

        Job saved = jobRepository.save(job);

        return new JobResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getLocation(),
                saved.getSalary(),
                saved.getCompany().getId()
        );
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(j -> new JobResponse(
                        j.getId(),
                        j.getTitle(),
                        j.getDescription(),
                        j.getLocation(),
                        j.getSalary(),
                        j.getCompany().getId()
                ))
                .toList();
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Job not found"));

        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSalary(),
                job.getCompany().getId()
        );
    }
}
