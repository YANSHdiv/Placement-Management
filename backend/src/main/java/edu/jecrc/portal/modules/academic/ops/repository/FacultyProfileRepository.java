package edu.jecrc.portal.modules.academic.ops.repository;

import edu.jecrc.portal.modules.academic.ops.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, UUID> {
    Optional<FacultyProfile> findByUserId(UUID userId);
    Optional<FacultyProfile> findByFacultyCodeIgnoreCase(String facultyCode);
    Optional<FacultyProfile> findByUserEmailIgnoreCase(String email);
    List<FacultyProfile> findByDepartmentIdAndIsActiveTrue(Integer departmentId);
    List<FacultyProfile> findByIsActiveTrue();
}
