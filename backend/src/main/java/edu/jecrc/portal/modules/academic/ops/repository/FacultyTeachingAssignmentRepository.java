package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.FacultyTeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyTeachingAssignmentRepository extends JpaRepository<FacultyTeachingAssignment, UUID> {
    List<FacultyTeachingAssignment> findByFacultyIdAndIsActiveTrue(UUID facultyId);
    List<FacultyTeachingAssignment> findByBatchIdAndSemesterAndSectionAndIsActiveTrue(Integer batchId, Integer semester, String section);
    Optional<FacultyTeachingAssignment> findByFacultyIdAndSubjectIdAndBatchIdAndSectionAndSemesterAndAcademicYear(
            UUID facultyId, Integer subjectId, Integer batchId, String section, Integer semester, String academicYear
    );
}
