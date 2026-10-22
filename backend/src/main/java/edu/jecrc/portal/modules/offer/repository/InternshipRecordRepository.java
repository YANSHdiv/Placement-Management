package edu.jecrc.portal.modules.offer.repository;

import edu.jecrc.portal.modules.offer.entity.InternshipRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InternshipRecordRepository extends JpaRepository<InternshipRecord, UUID> {

    List<InternshipRecord> findByStudentId(UUID studentId);

    List<InternshipRecord> findByFacultyMentorId(UUID facultyMentorId);

    @Query("SELECT i FROM InternshipRecord i WHERE " +
           "(:studentId IS NULL OR i.student.id = :studentId) AND " +
           "(:companyId IS NULL OR i.company.id = :companyId) AND " +
           "(:status IS NULL OR i.internshipStatus = :status) AND " +
           "(:facultyMentorId IS NULL OR i.facultyMentor.id = :facultyMentorId)")
    Page<InternshipRecord> searchInternships(
            @Param("studentId") UUID studentId,
            @Param("companyId") UUID companyId,
            @Param("status") String status,
            @Param("facultyMentorId") UUID facultyMentorId,
            Pageable pageable
    );
}
