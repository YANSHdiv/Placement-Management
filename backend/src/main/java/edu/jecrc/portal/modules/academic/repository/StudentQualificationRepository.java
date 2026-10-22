package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.StudentQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentQualificationRepository extends JpaRepository<StudentQualification, Integer> {

    List<StudentQualification> findByStudentId(UUID studentId);

    List<StudentQualification> findByStudentIdOrderByPassingYearAsc(UUID studentId);

    Optional<StudentQualification> findByStudentIdAndQualificationType(UUID studentId, String qualificationType);

    boolean existsByStudentIdAndQualificationType(UUID studentId, String qualificationType);
}
