package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.ApplicationResponse;
import com.vivek.jobportal.dto.EmployerApplicationResponse;
import com.vivek.jobportal.dto.UpdateApplicationStatusRequest;
import com.vivek.jobportal.entity.*;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.exception.NotFoundException;
import com.vivek.jobportal.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse apply(Long jobId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getRole() != Role.JOB_SEEKER) {
            throw new BadRequestException("Only job seekers can apply");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found"));

        if (applicationRepository.existsByJobIdAndUserId(jobId, user.getId())) {
            throw new BadRequestException("You already applied to this job");
        }

        Application application = Application.builder()
                .job(job)
                .user(user)
                .build();

        Application saved = applicationRepository.save(application);

        return new ApplicationResponse(
                saved.getId(),
                job.getId(),
                saved.getStatus().name(),
                saved.getAppliedAt()
        );
    }

    public List<ApplicationResponse> myApplications(String email) {
        return applicationRepository.findByUserEmail(email)
                .stream()
                .map(a -> new ApplicationResponse(
                        a.getId(),
                        a.getJob().getId(),
                        a.getStatus().name(),
                        a.getAppliedAt()
                ))
                .toList();
    }

    public List<EmployerApplicationResponse> applicationsForMyJobs(String employerEmail) {

        return applicationRepository
                .findByJobCompanyCreatedByEmailOrderByAppliedAtDesc(employerEmail)
                .stream()
                .map(this::toEmployerResponse)
                .toList();
    }

    public EmployerApplicationResponse updateApplicationStatus(Long applicationId,
                                                               UpdateApplicationStatusRequest request,
                                                               String employerEmail) {
        Application application = applicationRepository.findByIdAndJobCompanyCreatedByEmail(applicationId, employerEmail)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validateStatusChange(application.getStatus(), request.status());
        application.setStatus(request.status());

        return toEmployerResponse(applicationRepository.save(application));
    }

    private void validateStatusChange(ApplicationStatus currentStatus, ApplicationStatus nextStatus) {
        if (currentStatus == nextStatus) {
            throw new BadRequestException("Application is already in the requested status");
        }
        if (currentStatus == ApplicationStatus.REJECTED) {
            throw new BadRequestException("Rejected applications cannot be updated");
        }
    }

    private EmployerApplicationResponse toEmployerResponse(Application app) {
        return new EmployerApplicationResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getUser().getName(),
                app.getUser().getEmail(),
                app.getStatus().name(),
                app.getAppliedAt()
        );
    }
}
