package com.vivek.jobportal.repository;

import com.vivek.jobportal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByCompanyId(long companyId);

    @Query("""
            SELECT j
            FROM Job j
            WHERE (:companyId IS NULL OR j.company.id = :companyId)
              AND (:minSalary IS NULL OR j.salary >= :minSalary)
              AND (:maxSalary IS NULL OR j.salary <= :maxSalary)
              AND (:keyword IS NULL OR
                   LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(COALESCE(j.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR LOWER(COALESCE(j.location, '')) LIKE LOWER(CONCAT('%', :location, '%')))
            """)
    Page<Job> search(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("companyId") Long companyId,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            Pageable pageable
    );

    Page<Job> findByCompanyCreatedByEmail(String email, Pageable pageable);
}
