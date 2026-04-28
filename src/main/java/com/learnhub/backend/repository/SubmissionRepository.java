package com.learnhub.backend.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnhub.backend.entity.Submission;
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserId(Long userId);
    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByAssignmentCourseId(Long courseId);
}
