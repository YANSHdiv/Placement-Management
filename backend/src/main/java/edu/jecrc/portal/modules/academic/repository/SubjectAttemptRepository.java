package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.SubjectAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectAttemptRepository extends JpaRepository<SubjectAttempt, Integer> {

    List<SubjectAttempt> findByStudentIdOrderBySemesterNumberAscAttemptNumberAsc(UUID studentId);

    List<SubjectAttempt> findByStudentIdAndIsBacklogTrue(UUID studentId);

    List<SubjectAttempt> findByStudentIdAndIsBacklogTrueAndIsClearedFalse(UUID studentId);

    List<SubjectAttempt> findByResultId(Integer resultId);
}
