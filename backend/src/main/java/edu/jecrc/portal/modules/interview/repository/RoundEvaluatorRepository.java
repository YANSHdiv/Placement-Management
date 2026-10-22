package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.RoundEvaluator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundEvaluatorRepository extends JpaRepository<RoundEvaluator, UUID> {

    List<RoundEvaluator> findByRoundId(UUID roundId);

    Optional<RoundEvaluator> findByRoundIdAndUserId(UUID roundId, UUID userId);

    boolean existsByRoundIdAndUserId(UUID roundId, UUID userId);

    void deleteByRoundIdAndUserId(UUID roundId, UUID userId);

    @Query("SELECT re FROM RoundEvaluator re " +
           "JOIN FETCH re.user " +
           "WHERE re.round.id = :roundId")
    List<RoundEvaluator> findWithUsersByRoundId(@Param("roundId") UUID roundId);
}
