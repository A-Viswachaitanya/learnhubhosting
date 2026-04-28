package com.learnhub.backend.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnhub.backend.entity.CourseContent;
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findByCourseId(Long courseId);
}
