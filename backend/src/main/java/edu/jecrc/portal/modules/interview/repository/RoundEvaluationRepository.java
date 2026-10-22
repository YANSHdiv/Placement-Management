package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.RoundEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundEvaluationRepository extends JpaRepository<RoundEvaluation, UUID> {

    List<RoundEvaluation> findByCandidateId(UUID candidateId);

    Optional<RoundEvaluation> findByCandidateIdAndEvaluatorUserId(UUID candidateId, UUID evaluatorUserId);

    boolean existsByCandidateIdAndEvaluatorUserId(UUID candidateId, UUID evaluatorUserId);

    @Query("SELECT re FROM RoundEvaluation re " +
           "JOIN FETCH re.candidate rc " +
           "JOIN FETCH rc.round r " +
           "WHERE r.id = :roundId")
    List<RoundEvaluation> findByRoundId(@Param("roundId") UUID roundId);
}
