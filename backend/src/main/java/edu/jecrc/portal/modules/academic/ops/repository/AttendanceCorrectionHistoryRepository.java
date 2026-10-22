package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.AttendanceCorrectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceCorrectionHistoryRepository extends JpaRepository<AttendanceCorrectionHistory, UUID> {
    List<AttendanceCorrectionHistory> findByRecordIdOrderByCreatedAtDesc(UUID recordId);
    List<AttendanceCorrectionHistory> findBySessionIdOrderByCreatedAtDesc(UUID sessionId);
    List<AttendanceCorrectionHistory> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
