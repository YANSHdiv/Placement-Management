package edu.jecrc.portal.modules.academic.repository;

import edu.jecrc.portal.modules.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByUniversityStudentId(String universityStudentId);

    Optional<Student> findByRollNumber(String rollNumber);

    Optional<Student> findByUniversityEmail(String universityEmail);

    Optional<Student> findByUniversityEmailIgnoreCase(String universityEmail);

    Optional<Student> findByUserId(UUID userId);

    boolean existsByUniversityStudentId(String universityStudentId);

    boolean existsByRollNumber(String rollNumber);

    boolean existsByUniversityEmail(String universityEmail);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Student> findByDepartmentId(Integer departmentId);

    List<Student> findByBatchId(Integer batchId);

    List<Student> findByBatchIdAndCurrentSemesterAndSectionIgnoreCase(Integer batchId, Integer currentSemester, String section);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Student s LEFT JOIN FETCH s.department LEFT JOIN FETCH s.program LEFT JOIN FETCH s.batch WHERE s.id = :id")
    Optional<Student> findWithDetailsById(@org.springframework.data.repository.query.Param("id") UUID id);
}
