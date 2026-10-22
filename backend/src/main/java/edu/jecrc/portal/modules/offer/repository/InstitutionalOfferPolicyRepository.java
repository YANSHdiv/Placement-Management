package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.InstitutionalOfferPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionalOfferPolicyRepository extends JpaRepository<InstitutionalOfferPolicy, UUID> {
    Optional<InstitutionalOfferPolicy> findByPolicyCode(String policyCode);
    Optional<InstitutionalOfferPolicy> findFirstByIsActiveTrueOrderByCreatedAtDesc();
}
