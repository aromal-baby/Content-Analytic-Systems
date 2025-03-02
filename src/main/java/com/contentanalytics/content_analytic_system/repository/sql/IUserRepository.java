package com.contentanalytics.content_analytic_system.repository.sql;

import com.contentanalytics.content_analytic_system.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername (String username);
    Optional<User> findByEmail (String email);
    boolean existsByUsername (String username);
    boolean existsByEmail (String email);

}
