package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.OfferDecisionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferDecisionHistoryRepository extends JpaRepository<OfferDecisionHistory, UUID> {
    List<OfferDecisionHistory> findByOfferIdOrderByCreatedAtDesc(UUID offerId);
}
