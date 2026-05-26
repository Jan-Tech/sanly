package com.sanly.registry.repository;

import com.sanly.registry.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByNationalId(String nationalId);

    boolean existsByUsername(String username);
}
