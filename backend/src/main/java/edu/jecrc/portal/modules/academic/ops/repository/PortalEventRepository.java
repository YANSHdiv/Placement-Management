package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.PortalEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PortalEventRepository extends JpaRepository<PortalEvent, UUID> {

    List<PortalEvent> findByStatusOrderByEventDateAscStartTimeAsc(String status);

    @Query("SELECT e FROM PortalEvent e WHERE " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:eventType IS NULL OR e.eventType = :eventType) AND " +
            "(:fromDate IS NULL OR e.eventDate >= :fromDate) " +
            "ORDER BY e.eventDate ASC, e.startTime ASC")
    Page<PortalEvent> findFiltered(
            @Param("status") String status,
            @Param("eventType") String eventType,
            @Param("fromDate") LocalDate fromDate,
            Pageable pageable
    );
}
