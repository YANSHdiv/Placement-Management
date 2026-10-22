package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {
    List<EventRegistration> findByEventId(UUID eventId);
    List<EventRegistration> findByStudentId(UUID studentId);
    Optional<EventRegistration> findByEventIdAndStudentId(UUID eventId, UUID studentId);
    long countByEventId(UUID eventId);
}
