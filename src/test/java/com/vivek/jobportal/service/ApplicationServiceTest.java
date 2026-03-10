package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.EmployerApplicationResponse;
import com.vivek.jobportal.dto.UpdateApplicationStatusRequest;
import com.vivek.jobportal.entity.Application;
import com.vivek.jobportal.entity.ApplicationStatus;
import com.vivek.jobportal.entity.Company;
import com.vivek.jobportal.entity.Job;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.exception.NotFoundException;
import com.vivek.jobportal.repository.ApplicationRepository;
import com.vivek.jobportal.repository.JobRepository;
import com.vivek.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void updateApplicationStatusAllowsEmployerToShortlist() {
        Application application = application(ApplicationStatus.APPLIED);

        when(applicationRepository.findByIdAndJobCompanyCreatedByEmail(5L, "employer@test.com"))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        EmployerApplicationResponse response = applicationService.updateApplicationStatus(
                5L,
                new UpdateApplicationStatusRequest(ApplicationStatus.SHORTLISTED),
                "employer@test.com"
        );

        assertEquals("SHORTLISTED", response.status());
    }

    @Test
    void updateApplicationStatusRejectsSameStatus() {
        Application application = application(ApplicationStatus.SHORTLISTED);

        when(applicationRepository.findByIdAndJobCompanyCreatedByEmail(5L, "employer@test.com"))
                .thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class, () -> applicationService.updateApplicationStatus(
                5L,
                new UpdateApplicationStatusRequest(ApplicationStatus.SHORTLISTED),
                "employer@test.com"
        ));
    }

    @Test
    void updateApplicationStatusRejectsRejectedApplicationChanges() {
        Application application = application(ApplicationStatus.REJECTED);

        when(applicationRepository.findByIdAndJobCompanyCreatedByEmail(5L, "employer@test.com"))
                .thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class, () -> applicationService.updateApplicationStatus(
                5L,
                new UpdateApplicationStatusRequest(ApplicationStatus.SHORTLISTED),
                "employer@test.com"
        ));
    }

    @Test
    void updateApplicationStatusRejectsUnknownOwnership() {
        when(applicationRepository.findByIdAndJobCompanyCreatedByEmail(5L, "employer@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> applicationService.updateApplicationStatus(
                5L,
                new UpdateApplicationStatusRequest(ApplicationStatus.REJECTED),
                "employer@test.com"
        ));
    }

    private Application application(ApplicationStatus status) {
        User applicant = User.builder().name("Vivek").email("applicant@test.com").build();
        Company company = Company.builder().id(3L).name("Acme").build();
        Job job = Job.builder().id(7L).title("Backend Engineer").company(company).build();

        return Application.builder()
                .id(5L)
                .job(job)
                .user(applicant)
                .status(status)
                .appliedAt(LocalDateTime.now())
                .build();
    }
}
