package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    boolean existsByJobProfileIdAndStudentId(UUID jobProfileId, UUID studentId);

    Optional<JobApplication> findByJobProfileIdAndStudentId(UUID jobProfileId, UUID studentId);

    java.util.List<JobApplication> findByStudentId(UUID studentId);

    Page<JobApplication> findByStudentIdOrderByAppliedAtDesc(UUID studentId, Pageable pageable);

    @Query("SELECT a FROM JobApplication a WHERE " +
           "(:jobProfileId IS NULL OR a.jobProfile.id = :jobProfileId) AND " +
           "(:status IS NULL OR :status = '' OR a.applicationStatus = :status) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(a.student.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.student.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.student.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<JobApplication> searchApplications(
            @Param("jobProfileId") UUID jobProfileId,
            @Param("status") String status,
            @Param("query") String query,
            Pageable pageable
    );
}
