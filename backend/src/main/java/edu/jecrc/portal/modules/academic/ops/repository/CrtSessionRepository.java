package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.CrtSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CrtSessionRepository extends JpaRepository<CrtSession, UUID> {
    List<CrtSession> findByCrtProgramIdOrderBySessionDateAscStartTimeAsc(UUID programId);
    List<CrtSession> findByStatusOrderBySessionDateAscStartTimeAsc(String status);
}
