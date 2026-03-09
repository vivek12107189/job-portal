package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.CompanyResponse;
import com.vivek.jobportal.dto.CreateCompanyRequest;
import com.vivek.jobportal.entity.Company;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.repository.CompanyRepository;
import com.vivek.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository,
                          UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public CompanyResponse createCompany(CreateCompanyRequest request, String email) {

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!employer.getRole().name().equals("EMPLOYER")) {
            throw new BadRequestException("Only employers can create companies");
        }

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .createdBy(employer)
                .build();

        Company saved = companyRepository.save(company);

        return new CompanyResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getLocation()
        );
    }

    public List<CompanyResponse> myCompanies(String email) {
        return companyRepository.findByCreatedByEmail(email)
                .stream()
                .map(c -> new CompanyResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getLocation()
                ))
                .toList();
    }
}
