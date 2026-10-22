package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.AttendanceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    List<AttendanceSession> findByFacultyIdAndSessionDate(UUID facultyId, LocalDate sessionDate);

    List<AttendanceSession> findByBatchIdAndSemesterAndSectionAndSessionDate(
            Integer batchId, Integer semester, String section, LocalDate sessionDate
    );

    List<AttendanceSession> findBySubjectIdAndBatchIdAndSemesterAndSection(
            Integer subjectId, Integer batchId, Integer semester, String section
    );

    @Query("SELECT s FROM AttendanceSession s WHERE " +
            "(:facultyId IS NULL OR s.faculty.id = :facultyId) AND " +
            "(:subjectId IS NULL OR s.subject.id = :subjectId) AND " +
            "(:batchId IS NULL OR s.batch.id = :batchId) AND " +
            "(:status IS NULL OR s.sessionStatus = :status)")
    Page<AttendanceSession> findFiltered(
            @Param("facultyId") UUID facultyId,
            @Param("subjectId") Integer subjectId,
            @Param("batchId") Integer batchId,
            @Param("status") String status,
            Pageable pageable
    );
}
