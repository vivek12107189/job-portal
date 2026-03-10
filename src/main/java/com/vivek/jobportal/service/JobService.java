package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.CreateJobRequest;
import com.vivek.jobportal.dto.JobSearchRequest;
import com.vivek.jobportal.dto.JobResponse;
import com.vivek.jobportal.dto.PageResponse;
import com.vivek.jobportal.dto.UpdateJobRequest;
import com.vivek.jobportal.entity.Company;
import com.vivek.jobportal.entity.Job;
import com.vivek.jobportal.entity.Role;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.exception.NotFoundException;
import com.vivek.jobportal.repository.CompanyRepository;
import com.vivek.jobportal.repository.JobRepository;
import com.vivek.jobportal.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class JobService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "salary", "title");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

        if (employer.getRole() != Role.EMPLOYER) {
            throw new BadRequestException("Only employers can create jobs");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Company not found"));

        if (!company.getCreatedBy().getId().equals(employer.getId())) {
            throw new BadRequestException("You are not allowed to post jobs for this company");
        }

        Job job = Job.builder()
                .title(request.getTitle().trim())
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
                saved.getCompany().getId(),
                saved.getCompany().getName(),
                saved.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(saved.getCreatedAt())
        );
    }

    public PageResponse<JobResponse> searchJobs(JobSearchRequest request) {
        validateSalaryRange(request.minSalary(), request.maxSalary());

        Page<Job> jobs = jobRepository.search(
                normalize(request.keyword()),
                normalize(request.location()),
                request.companyId(),
                request.minSalary(),
                request.maxSalary(),
                buildPageRequest(request)
        );

        return toPageResponse(jobs);
    }

    public PageResponse<JobResponse> getEmployerJobs(String email) {
        Page<Job> jobs = jobRepository.findByCompanyCreatedByEmail(
                email,
                PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")))
        );

        return toPageResponse(jobs);
    }

    public JobResponse updateJob(Long jobId, UpdateJobRequest request, String email) {
        Job job = getEmployerOwnedJob(jobId, email);
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());

        return toResponse(jobRepository.save(job));
    }

    public void deleteJob(Long jobId, String email) {
        Job job = getEmployerOwnedJob(jobId, email);
        jobRepository.delete(job);
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found"));

        return toResponse(job);
    }

    private void validateSalaryRange(Double minSalary, Double maxSalary) {
        if (minSalary != null && maxSalary != null && minSalary > maxSalary) {
            throw new BadRequestException("minSalary must be less than or equal to maxSalary");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSalary(),
                job.getCompany().getId(),
                job.getCompany().getName(),
                job.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(job.getCreatedAt())
        );
    }

    private Job getEmployerOwnedJob(Long jobId, String email) {
        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (employer.getRole() != Role.EMPLOYER) {
            throw new BadRequestException("Only employers can manage jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found"));

        if (!job.getCompany().getCreatedBy().getId().equals(employer.getId())) {
            throw new BadRequestException("You are not allowed to manage this job");
        }

        return job;
    }

    private PageRequest buildPageRequest(JobSearchRequest request) {
        String sortBy = request.resolvedSortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Unsupported sortBy value");
        }

        Sort.Direction direction = switch (request.resolvedDirection().trim().toLowerCase()) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new BadRequestException("Unsupported direction value");
        };

        return PageRequest.of(
                request.resolvedPage(),
                request.resolvedSize(),
                Sort.by(direction, sortBy).and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    private PageResponse<JobResponse> toPageResponse(Page<Job> jobs) {
        return new PageResponse<>(
                jobs.getContent().stream().map(this::toResponse).toList(),
                jobs.getNumber(),
                jobs.getSize(),
                jobs.getTotalElements(),
                jobs.getTotalPages(),
                jobs.isFirst(),
                jobs.isLast()
        );
    }
}
