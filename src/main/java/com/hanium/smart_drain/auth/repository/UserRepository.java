package com.hanium.smart_drain.auth.repository;

import com.hanium.smart_drain.auth.entity.User;
import com.hanium.smart_drain.auth.entity.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findByRoleOrderByUserIdAsc(UserRole role);
}
