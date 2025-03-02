package com.contentanalytics.content_analytic_system.repository.sql;

import com.contentanalytics.content_analytic_system.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(Role.ERole name);

}
