package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.InterviewRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRoundRepository extends JpaRepository<InterviewRound, UUID> {

    List<InterviewRound> findByJobProfileIdOrderBySequenceOrderAsc(UUID jobProfileId);

    Optional<InterviewRound> findByJobProfileIdAndSequenceOrder(UUID jobProfileId, Integer sequenceOrder);

    boolean existsByJobProfileIdAndSequenceOrder(UUID jobProfileId, Integer sequenceOrder);

    @Query("SELECT r FROM InterviewRound r " +
           "LEFT JOIN FETCH r.jobProfile jp " +
           "LEFT JOIN FETCH jp.drive " +
           "WHERE r.id = :id")
    Optional<InterviewRound> findWithDetailsById(@Param("id") UUID id);
}
