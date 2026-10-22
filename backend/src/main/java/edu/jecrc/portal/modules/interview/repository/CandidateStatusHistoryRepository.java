package edu.jecrc.portal.modules.interview.repository;

import edu.jecrc.portal.modules.interview.entity.CandidateStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateStatusHistoryRepository extends JpaRepository<CandidateStatusHistory, UUID> {

    List<CandidateStatusHistory> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
}
