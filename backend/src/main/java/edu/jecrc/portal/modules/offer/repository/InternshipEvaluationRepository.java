package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.InternshipEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InternshipEvaluationRepository extends JpaRepository<InternshipEvaluation, UUID> {
    List<InternshipEvaluation> findByInternshipIdOrderByEvaluatedAtDesc(UUID internshipId);
}
