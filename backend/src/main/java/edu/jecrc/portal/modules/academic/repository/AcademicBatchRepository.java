package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicBatchRepository extends JpaRepository<AcademicBatch, Integer> {

    List<AcademicBatch> findByProgramId(Integer programId);

    List<AcademicBatch> findByIsActiveTrue();

    Optional<AcademicBatch> findByProgramIdAndGraduationYear(Integer programId, Integer graduationYear);
}
