package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicProgramRepository extends JpaRepository<AcademicProgram, Integer> {

    Optional<AcademicProgram> findByProgramCode(String programCode);

    List<AcademicProgram> findByDepartmentId(Integer departmentId);

    List<AcademicProgram> findByIsActiveTrue();
}
