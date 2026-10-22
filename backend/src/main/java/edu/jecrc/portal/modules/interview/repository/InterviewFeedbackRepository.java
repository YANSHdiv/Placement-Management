package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.InterviewFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, UUID> {

    List<InterviewFeedback> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);

    List<InterviewFeedback> findByRoundIdOrderByCreatedAtDesc(UUID roundId);

    List<InterviewFeedback> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    @Query(value = "SELECT f FROM InterviewFeedback f " +
           "JOIN FETCH f.submittedBy u " +
           "LEFT JOIN FETCH f.company c " +
           "WHERE (:companyId IS NULL OR f.company.id = :companyId) " +
           "AND (:feedbackType IS NULL OR f.feedbackType = :feedbackType) " +
           "AND (:moderationStatus IS NULL OR f.moderationStatus = :moderationStatus)",
           countQuery = "SELECT count(f) FROM InterviewFeedback f " +
           "WHERE (:companyId IS NULL OR f.company.id = :companyId) " +
           "AND (:feedbackType IS NULL OR f.feedbackType = :feedbackType) " +
           "AND (:moderationStatus IS NULL OR f.moderationStatus = :moderationStatus)")
    Page<InterviewFeedback> searchFeedback(
            @Param("companyId") UUID companyId,
            @Param("feedbackType") String feedbackType,
            @Param("moderationStatus") String moderationStatus,
            Pageable pageable
    );
}
