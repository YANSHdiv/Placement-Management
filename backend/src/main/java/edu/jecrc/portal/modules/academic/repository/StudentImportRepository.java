package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.StudentImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentImportRepository extends JpaRepository<StudentImport, UUID> {

    Optional<StudentImport> findByIdAndImportedById(UUID importId, UUID userId);
}
