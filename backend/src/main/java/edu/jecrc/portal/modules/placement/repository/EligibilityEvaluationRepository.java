package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.EligibilityEvaluation;
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
public interface EligibilityEvaluationRepository extends JpaRepository<EligibilityEvaluation, UUID> {

    Optional<EligibilityEvaluation> findFirstByStudentIdAndJobProfileIdOrderByEvaluatedAtDesc(UUID studentId, UUID jobProfileId);

    List<EligibilityEvaluation> findByStudentIdAndJobProfileIdOrderByEvaluatedAtDesc(UUID studentId, UUID jobProfileId);

    Page<EligibilityEvaluation> findByStudentIdOrderByEvaluatedAtDesc(UUID studentId, Pageable pageable);

    @Query("SELECT e FROM EligibilityEvaluation e WHERE " +
           "e.jobProfile.id = :jobProfileId AND " +
           "(:outcome IS NULL OR :outcome = '' OR e.outcome = :outcome)")
    Page<EligibilityEvaluation> findByJobProfileIdAndOutcome(
            @Param("jobProfileId") UUID jobProfileId,
            @Param("outcome") String outcome,
            Pageable pageable
    );
}
