package com.learnhub.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learnhub.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Derived query methods based on syllabus
    Optional<User> findByEmail(String email);
}
