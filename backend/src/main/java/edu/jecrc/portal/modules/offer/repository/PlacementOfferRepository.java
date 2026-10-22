package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
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
public interface PlacementOfferRepository extends JpaRepository<PlacementOffer, UUID> {

    Optional<PlacementOffer> findByOfferReferenceNumber(String offerReferenceNumber);

    List<PlacementOffer> findByStudentId(UUID studentId);

    List<PlacementOffer> findByStudentIdAndOfferStatusIn(UUID studentId, List<String> statuses);

    Optional<PlacementOffer> findByApplicationId(UUID applicationId);

    @Query("SELECT o FROM PlacementOffer o WHERE " +
           "(:studentId IS NULL OR o.student.id = :studentId) AND " +
           "(:companyId IS NULL OR o.company.id = :companyId) AND " +
           "(:offerStatus IS NULL OR o.offerStatus = :offerStatus)")
    Page<PlacementOffer> searchOffers(
            @Param("studentId") UUID studentId,
            @Param("companyId") UUID companyId,
            @Param("offerStatus") String offerStatus,
            Pageable pageable
    );
}
