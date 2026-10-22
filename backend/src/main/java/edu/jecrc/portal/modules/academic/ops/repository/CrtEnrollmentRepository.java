package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.CrtEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CrtEnrollmentRepository extends JpaRepository<CrtEnrollment, UUID> {
    List<CrtEnrollment> findBySessionId(UUID sessionId);
    List<CrtEnrollment> findByStudentId(UUID studentId);
    Optional<CrtEnrollment> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);
    long countBySessionId(UUID sessionId);
}
