package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.CrtProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CrtProgramRepository extends JpaRepository<CrtProgram, UUID> {
    Optional<CrtProgram> findByProgramCodeIgnoreCase(String programCode);
    List<CrtProgram> findByIsActiveTrueOrderByProgramNameAsc();
    List<CrtProgram> findByAcademicYearAndIsActiveTrue(String academicYear);
}
