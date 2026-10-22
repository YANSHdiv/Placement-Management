package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.PpoRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PpoRecordRepository extends JpaRepository<PpoRecord, UUID> {

    Optional<PpoRecord> findByInternshipId(UUID internshipId);

    List<PpoRecord> findByStudentId(UUID studentId);

    @Query("SELECT p FROM PpoRecord p WHERE " +
           "(:companyId IS NULL OR p.company.id = :companyId) AND " +
           "(:ppoStatus IS NULL OR p.ppoStatus = :ppoStatus) AND " +
           "(:studentId IS NULL OR p.student.id = :studentId)")
    Page<PpoRecord> searchPpoRecords(
            @Param("companyId") UUID companyId,
            @Param("ppoStatus") String ppoStatus,
            @Param("studentId") UUID studentId,
            Pageable pageable
    );
}
