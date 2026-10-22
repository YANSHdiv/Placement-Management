package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.RoundCandidate;
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
public interface RoundCandidateRepository extends JpaRepository<RoundCandidate, UUID> {

    List<RoundCandidate> findByRoundId(UUID roundId);

    Optional<RoundCandidate> findByRoundIdAndApplicationId(UUID roundId, UUID applicationId);

    boolean existsByRoundIdAndApplicationId(UUID roundId, UUID applicationId);

    @Query("SELECT rc FROM RoundCandidate rc " +
           "JOIN FETCH rc.application app " +
           "JOIN FETCH app.student s " +
           "WHERE rc.round.id = :roundId")
    List<RoundCandidate> findWithDetailsByRoundId(@Param("roundId") UUID roundId);

    @Query(value = "SELECT rc FROM RoundCandidate rc " +
           "JOIN FETCH rc.application app " +
           "JOIN FETCH app.student s " +
           "WHERE rc.round.id = :roundId AND (:status IS NULL OR rc.candidateRoundStatus = :status)",
           countQuery = "SELECT count(rc) FROM RoundCandidate rc WHERE rc.round.id = :roundId AND (:status IS NULL OR rc.candidateRoundStatus = :status)")
    Page<RoundCandidate> findByRoundIdAndStatusPageable(@Param("roundId") UUID roundId, @Param("status") String status, Pageable pageable);

    @Query("SELECT rc FROM RoundCandidate rc " +
           "JOIN FETCH rc.round r " +
           "JOIN FETCH rc.application app " +
           "JOIN FETCH app.student s " +
           "WHERE rc.id = :id")
    Optional<RoundCandidate> findWithDetailsById(@Param("id") UUID id);

    List<RoundCandidate> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);
}
