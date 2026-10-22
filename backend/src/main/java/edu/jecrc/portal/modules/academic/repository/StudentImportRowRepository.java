package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.StudentImportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentImportRowRepository extends JpaRepository<StudentImportRow, Integer> {

    List<StudentImportRow> findByStudentImportIdOrderByRowNumberAsc(UUID importId);

    List<StudentImportRow> findByStudentImportIdAndIsValidFalse(UUID importId);
}
