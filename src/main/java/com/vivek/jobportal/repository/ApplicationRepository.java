package com.vivek.jobportal.repository;

import com.vivek.jobportal.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndUserId(Long jobId, Long userId);

    List<Application> findByUserEmail(String email);
    List<Application> findByJobCompanyCreatedByEmailOrderByAppliedAtDesc(String email);

    Optional<Application> findByIdAndJobCompanyCreatedByEmail(Long id, String email);
}
