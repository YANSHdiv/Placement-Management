package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    List<TimetableSlot> findByBatchIdAndSemesterAndSectionAndAcademicYearAndIsActiveTrue(
            Integer batchId, Integer semester, String section, String academicYear
    );

    List<TimetableSlot> findByFacultyIdAndAcademicYearAndIsActiveTrue(UUID facultyId, String academicYear);

    List<TimetableSlot> findByClassroomIdAndAcademicYearAndIsActiveTrue(Integer classroomId, String academicYear);

    List<TimetableSlot> findByAcademicYearAndIsActiveTrue(String academicYear);

    @Query("SELECT t FROM TimetableSlot t WHERE t.batch.id = :batchId AND t.semester = :semester AND t.section = :section AND t.isActive = true")
    List<TimetableSlot> findByCohort(@Param("batchId") Integer batchId, @Param("semester") Integer semester, @Param("section") String section);
}
