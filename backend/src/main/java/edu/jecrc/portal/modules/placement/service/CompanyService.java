package edu.jecrc.portal.modules.placement.service;

import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.placement.dto.CompanyCreateRequest;
import edu.jecrc.portal.modules.placement.dto.CompanyDto;
import edu.jecrc.portal.modules.placement.dto.CompanyUpdateRequest;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public CompanyDto createCompany(CompanyCreateRequest req, String userEmail) {
        String code = req.getCompanyCode().trim().toUpperCase();
        if (companyRepository.existsByCompanyCodeIgnoreCase(code)) {
            throw new BadRequestException("Company with code '" + code + "' already exists");
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        Company company = Company.builder()
                .companyName(req.getCompanyName().trim())
                .companyCode(code)
                .website(req.getWebsite())
                .industry(req.getIndustry())
                .description(req.getDescription())
                .recruiterName(req.getRecruiterName())
                .recruiterEmail(req.getRecruiterEmail())
                .recruiterPhone(req.getRecruiterPhone())
                .companyStatus("ACTIVE")
                .internalNotes(req.getInternalNotes())
                .createdBy(user)
                .updatedBy(user)
                .build();

        company = companyRepository.save(company);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "CREATE_COMPANY",
                "Company",
                company.getId().toString(),
                Map.of("companyName", company.getCompanyName(), "companyCode", company.getCompanyCode()),
                null,
                null
        );

        return toDto(company, true);
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompany(UUID companyId, boolean includePrivateRecruiterInfo) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        return toDto(company, includePrivateRecruiterInfo);
    }

    @Transactional
    public CompanyDto updateCompany(UUID companyId, CompanyUpdateRequest req, String userEmail) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        company.setCompanyName(req.getCompanyName().trim());
        company.setWebsite(req.getWebsite());
        company.setIndustry(req.getIndustry());
        company.setDescription(req.getDescription());
        company.setRecruiterName(req.getRecruiterName());
        company.setRecruiterEmail(req.getRecruiterEmail());
        company.setRecruiterPhone(req.getRecruiterPhone());
        if (req.getCompanyStatus() != null && !req.getCompanyStatus().isBlank()) {
            company.setCompanyStatus(req.getCompanyStatus().trim().toUpperCase());
        }
        if (req.getInternalNotes() != null) {
            company.setInternalNotes(req.getInternalNotes());
        }
        company.setUpdatedBy(user);

        company = companyRepository.save(company);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "UPDATE_COMPANY",
                "Company",
                company.getId().toString(),
                Map.of("companyName", company.getCompanyName(), "status", company.getCompanyStatus()),
                null,
                null
        );

        return toDto(company, true);
    }

    @Transactional
    public CompanyDto archiveCompany(UUID companyId, String userEmail) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);
        company.setCompanyStatus("ARCHIVED");
        company.setUpdatedBy(user);
        company = companyRepository.save(company);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "ARCHIVE_COMPANY",
                "Company",
                company.getId().toString(),
                Map.of("companyCode", company.getCompanyCode()),
                null,
                null
        );

        return toDto(company, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyDto> searchCompanies(String query, String status, Pageable pageable, boolean includePrivateRecruiterInfo) {
        Page<Company> page = companyRepository.searchCompanies(query, status, pageable);
        Page<CompanyDto> dtoPage = page.map(c -> toDto(c, includePrivateRecruiterInfo));
        return PageResponse.from(dtoPage);
    }

    public CompanyDto toDto(Company c, boolean includePrivateRecruiterInfo) {
        return CompanyDto.builder()
                .id(c.getId())
                .companyName(c.getCompanyName())
                .companyCode(c.getCompanyCode())
                .website(c.getWebsite())
                .industry(c.getIndustry())
                .description(c.getDescription())
                .recruiterName(includePrivateRecruiterInfo ? c.getRecruiterName() : null)
                .recruiterEmail(includePrivateRecruiterInfo ? c.getRecruiterEmail() : null)
                .recruiterPhone(includePrivateRecruiterInfo ? c.getRecruiterPhone() : null)
                .companyStatus(c.getCompanyStatus())
                .internalNotes(includePrivateRecruiterInfo ? c.getInternalNotes() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
