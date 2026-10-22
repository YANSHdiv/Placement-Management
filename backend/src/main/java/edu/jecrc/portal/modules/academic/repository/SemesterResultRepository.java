package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.SemesterResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SemesterResultRepository extends JpaRepository<SemesterResult, Integer> {

    List<SemesterResult> findByStudentIdOrderBySemesterNumberAsc(UUID studentId);

    Optional<SemesterResult> findByStudentIdAndSemesterNumber(UUID studentId, Integer semesterNumber);

    boolean existsByStudentIdAndSemesterNumber(UUID studentId, Integer semesterNumber);
}
