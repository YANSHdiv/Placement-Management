package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.InternshipProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InternshipProgressLogRepository extends JpaRepository<InternshipProgressLog, UUID> {
    List<InternshipProgressLog> findByInternshipIdOrderByLogNumberAsc(UUID internshipId);
    Integer countByInternshipId(UUID internshipId);
}
