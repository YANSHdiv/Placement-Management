package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findBySessionId(UUID sessionId);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    List<AttendanceRecord> findByStudentId(UUID studentId);

    @Query("SELECT r FROM AttendanceRecord r JOIN r.session s " +
            "WHERE r.student.id = :studentId AND s.subject.id = :subjectId")
    List<AttendanceRecord> findByStudentIdAndSubjectId(
            @Param("studentId") UUID studentId,
            @Param("subjectId") Integer subjectId
    );

    @Query("SELECT r FROM AttendanceRecord r JOIN r.session s " +
            "WHERE s.batch.id = :batchId AND s.semester = :semester AND s.section = :section")
    List<AttendanceRecord> findByCohort(
            @Param("batchId") Integer batchId,
            @Param("semester") Integer semester,
            @Param("section") String section
    );
}
