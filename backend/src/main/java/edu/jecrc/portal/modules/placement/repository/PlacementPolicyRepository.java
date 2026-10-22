package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.PlacementPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlacementPolicyRepository extends JpaRepository<PlacementPolicy, UUID> {

    Optional<PlacementPolicy> findByPolicyCodeAndVersionNumber(String policyCode, Integer versionNumber);

    List<PlacementPolicy> findByPolicyCodeOrderByVersionNumberDesc(String policyCode);

    @Query("SELECT MAX(p.versionNumber) FROM PlacementPolicy p WHERE p.policyCode = :policyCode")
    Integer findMaxVersionNumberByPolicyCode(@Param("policyCode") String policyCode);

    @Query("SELECT p FROM PlacementPolicy p WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(p.policyName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.policyCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR p.policyStatus = :status)")
    Page<PlacementPolicy> searchPolicies(@Param("query") String query, @Param("status") String status, Pageable pageable);

    List<PlacementPolicy> findByPolicyStatus(String policyStatus);
}
