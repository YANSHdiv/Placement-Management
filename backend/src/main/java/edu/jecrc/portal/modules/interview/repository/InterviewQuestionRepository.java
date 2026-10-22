package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {

    @Query(value = "SELECT q FROM InterviewQuestion q " +
           "LEFT JOIN FETCH q.company c " +
           "WHERE (:query IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(q.category) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:questionType IS NULL OR q.questionType = :questionType) " +
           "AND (:companyId IS NULL OR (q.company IS NOT NULL AND q.company.id = :companyId)) " +
           "AND (:isArchived IS NULL OR q.isArchived = :isArchived)",
           countQuery = "SELECT count(q) FROM InterviewQuestion q " +
           "WHERE (:query IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(q.category) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:questionType IS NULL OR q.questionType = :questionType) " +
           "AND (:companyId IS NULL OR (q.company IS NOT NULL AND q.company.id = :companyId)) " +
           "AND (:isArchived IS NULL OR q.isArchived = :isArchived)")
    Page<InterviewQuestion> searchQuestions(
            @Param("query") String query,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("questionType") String questionType,
            @Param("companyId") UUID companyId,
            @Param("isArchived") Boolean isArchived,
            Pageable pageable
    );
}
