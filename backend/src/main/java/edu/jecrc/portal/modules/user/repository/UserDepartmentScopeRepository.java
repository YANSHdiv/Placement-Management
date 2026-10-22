package edu.jecrc.portal.modules.user.repository;

import edu.jecrc.portal.modules.user.entity.UserDepartmentScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDepartmentScopeRepository extends JpaRepository<UserDepartmentScope, Integer> {
    List<UserDepartmentScope> findByUserId(UUID userId);
}
