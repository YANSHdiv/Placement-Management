package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByCompanyCode(String companyCode);

    boolean existsByCompanyCodeIgnoreCase(String companyCode);

    @Query("SELECT c FROM Company c WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.companyCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR c.companyStatus = :status)")
    Page<Company> searchCompanies(@Param("query") String query, @Param("status") String status, Pageable pageable);
}
