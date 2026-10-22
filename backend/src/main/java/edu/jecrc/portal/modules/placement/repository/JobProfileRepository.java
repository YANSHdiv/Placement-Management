package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.JobProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobProfileRepository extends JpaRepository<JobProfile, UUID> {

    List<JobProfile> findByDriveId(UUID driveId);

    @Query("SELECT j FROM JobProfile j WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(j.profileTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(j.drive.company.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:driveId IS NULL OR j.drive.id = :driveId) AND " +
           "(:employmentType IS NULL OR :employmentType = '' OR j.employmentType = :employmentType)")
    Page<JobProfile> searchJobProfiles(
            @Param("query") String query,
            @Param("driveId") UUID driveId,
            @Param("employmentType") String employmentType,
            Pageable pageable
    );

    @Query("SELECT j FROM JobProfile j WHERE j.drive.driveStatus IN ('PUBLISHED', 'IN_PROGRESS')")
    Page<JobProfile> findPublishedJobProfiles(Pageable pageable);

    @Query("SELECT j FROM JobProfile j JOIN FETCH j.drive d JOIN FETCH d.company JOIN FETCH j.policy WHERE j.id = :id")
    java.util.Optional<JobProfile> findWithDetailsById(@Param("id") UUID id);
}
