package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.EligibilityOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EligibilityOverrideRepository extends JpaRepository<EligibilityOverride, UUID> {

    Optional<EligibilityOverride> findByEvaluationId(UUID evaluationId);

    List<EligibilityOverride> findByEvaluationStudentId(UUID studentId);
}
