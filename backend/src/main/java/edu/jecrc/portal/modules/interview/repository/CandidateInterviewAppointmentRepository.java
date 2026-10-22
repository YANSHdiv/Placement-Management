package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.CandidateInterviewAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateInterviewAppointmentRepository extends JpaRepository<CandidateInterviewAppointment, UUID> {

    List<CandidateInterviewAppointment> findByCandidateId(UUID candidateId);

    List<CandidateInterviewAppointment> findByStudentIdOrderByStartTimeAsc(UUID studentId);

    List<CandidateInterviewAppointment> findByEvaluatorUserIdOrderByStartTimeAsc(UUID evaluatorUserId);

    @Query("SELECT a FROM CandidateInterviewAppointment a " +
           "WHERE a.student.id = :studentId " +
           "AND a.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    List<CandidateInterviewAppointment> findConflictingStudentAppointments(
            @Param("studentId") UUID studentId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeId") UUID excludeId
    );

    @Query("SELECT a FROM CandidateInterviewAppointment a " +
           "WHERE a.evaluatorUser.id = :evaluatorId " +
           "AND a.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    List<CandidateInterviewAppointment> findConflictingEvaluatorAppointments(
            @Param("evaluatorId") UUID evaluatorId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeId") UUID excludeId
    );

    @Query("SELECT a FROM CandidateInterviewAppointment a " +
           "JOIN FETCH a.candidate rc " +
           "JOIN FETCH rc.round r " +
           "JOIN FETCH r.jobProfile jp " +
           "JOIN FETCH jp.drive d " +
           "JOIN FETCH d.company " +
           "WHERE a.student.id = :studentId " +
           "ORDER BY a.startTime ASC")
    List<CandidateInterviewAppointment> findWithDetailsByStudentId(@Param("studentId") UUID studentId);
}
