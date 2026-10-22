package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.AcademicSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicSubjectRepository extends JpaRepository<AcademicSubject, Integer> {
    Optional<AcademicSubject> findBySubjectCodeIgnoreCase(String subjectCode);
    List<AcademicSubject> findByDepartmentIdAndIsActiveTrue(Integer departmentId);
    List<AcademicSubject> findBySemesterAndIsActiveTrue(Integer semester);
    List<AcademicSubject> findByIsActiveTrueOrderBySubjectCodeAsc();
}
