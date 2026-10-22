package edu.jecrc.portal.modules.placement.repository;

import edu.jecrc.portal.modules.placement.entity.PlacementDrive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, UUID> {

    List<PlacementDrive> findByCompanyId(UUID companyId);

    @Query("SELECT d FROM PlacementDrive d WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(d.driveTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.company.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR d.driveStatus = :status) AND " +
           "(:academicYear IS NULL OR :academicYear = '' OR d.academicYear = :academicYear)")
    Page<PlacementDrive> searchDrives(
            @Param("query") String query,
            @Param("status") String status,
            @Param("academicYear") String academicYear,
            Pageable pageable
    );

    @Query("SELECT d FROM PlacementDrive d WHERE d.driveStatus IN :statuses")
    Page<PlacementDrive> findByDriveStatusIn(@Param("statuses") List<String> statuses, Pageable pageable);
}
