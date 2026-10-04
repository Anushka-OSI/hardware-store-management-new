package com.guruge.hardware.repository;

import com.guruge.hardware.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    List<User> findByStatus(String status);

    List<User> findByRoleId(Long roleId);
}
