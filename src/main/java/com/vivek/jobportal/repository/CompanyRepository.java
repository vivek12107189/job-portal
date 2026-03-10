package com.vivek.jobportal.repository;

import com.vivek.jobportal.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company,Long> {

    List<Company> findByCreatedByEmail(String email);

    boolean existsByNameIgnoreCase(String name);
}
