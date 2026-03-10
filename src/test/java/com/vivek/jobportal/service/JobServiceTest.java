package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.JobResponse;
import com.vivek.jobportal.dto.JobSearchRequest;
import com.vivek.jobportal.dto.PageResponse;
import com.vivek.jobportal.dto.UpdateJobRequest;
import com.vivek.jobportal.entity.Company;
import com.vivek.jobportal.entity.Job;
import com.vivek.jobportal.entity.Role;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.repository.CompanyRepository;
import com.vivek.jobportal.repository.JobRepository;
import com.vivek.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void searchJobsUsesPagingAndFilters() {
        Company company = Company.builder().id(7L).build();
        Job job = Job.builder()
                .id(11L)
                .title("Java Backend Developer")
                .description("Spring Boot role")
                .location("Bangalore")
                .salary(1200000.0)
                .company(company)
                .build();

        when(jobRepository.search(
                eq("java"),
                eq("bangalore"),
                eq(7L),
                eq(500000.0),
                eq(1500000.0),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(job)));

        PageResponse<JobResponse> response = jobService.searchJobs(
                new JobSearchRequest(" java ", " bangalore ", 7L, 500000.0, 1500000.0, "salary", "asc", 1, 5)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).search(
                eq("java"),
                eq("bangalore"),
                eq(7L),
                eq(500000.0),
                eq(1500000.0),
                pageableCaptor.capture()
        );

        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
        assertEquals(1, response.content().size());
        assertEquals("Java Backend Developer", response.content().get(0).title());
        assertEquals("salary: ASC,id: DESC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void searchJobsRejectsInvalidSalaryRange() {
        assertThrows(BadRequestException.class,
                () -> jobService.searchJobs(new JobSearchRequest(null, null, null, 2000.0, 1000.0, null, null, 0, 10)));
    }

    @Test
    void searchJobsRejectsUnsupportedSortField() {
        assertThrows(BadRequestException.class,
                () -> jobService.searchJobs(new JobSearchRequest(null, null, null, null, null, "companyId", "asc", 0, 10)));
    }

    @Test
    void updateJobAllowsOwningEmployer() {
        User employer = User.builder().id(3L).email("owner@test.com").role(Role.EMPLOYER).build();
        Company company = Company.builder().id(7L).name("Acme").createdBy(employer).build();
        Job job = Job.builder()
                .id(11L)
                .title("Old title")
                .description("Old desc")
                .location("Old")
                .salary(1000.0)
                .company(company)
                .build();
        UpdateJobRequest request = new UpdateJobRequest();
        request.setTitle("New title");
        request.setDescription("New desc");
        request.setLocation("New");
        request.setSalary(2000.0);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(java.util.Optional.of(employer));
        when(jobRepository.findById(11L)).thenReturn(java.util.Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.updateJob(11L, request, "owner@test.com");

        assertEquals("New title", response.title());
        assertEquals("Acme", response.companyName());
    }

    @Test
    void deleteJobRejectsNonOwner() {
        User employer = User.builder().id(3L).email("owner@test.com").role(Role.EMPLOYER).build();
        User otherEmployer = User.builder().id(4L).email("other@test.com").role(Role.EMPLOYER).build();
        Company company = Company.builder().id(7L).name("Acme").createdBy(otherEmployer).build();
        Job job = Job.builder().id(11L).company(company).build();

        when(userRepository.findByEmail("owner@test.com")).thenReturn(java.util.Optional.of(employer));
        when(jobRepository.findById(11L)).thenReturn(java.util.Optional.of(job));

        assertThrows(BadRequestException.class, () -> jobService.deleteJob(11L, "owner@test.com"));
    }
}
