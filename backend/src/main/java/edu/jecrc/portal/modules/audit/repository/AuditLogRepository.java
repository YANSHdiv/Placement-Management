package edu.jecrc.portal.modules.audit.repository;

import edu.jecrc.portal.modules.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByUserEmailContainingIgnoreCase(String userEmail, Pageable pageable);

    Page<AuditLog> findByActionAndUserEmailContainingIgnoreCase(String action, String userEmail, Pageable pageable);
}
