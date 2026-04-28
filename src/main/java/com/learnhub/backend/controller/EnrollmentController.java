package com.learnhub.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnhub.backend.dto.EnrollmentDTO;
import com.learnhub.backend.entity.Course;
import com.learnhub.backend.entity.Enrollment;
import com.learnhub.backend.entity.User;
import com.learnhub.backend.repository.CourseRepository;
import com.learnhub.backend.repository.EnrollmentRepository;
import com.learnhub.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<EnrollmentDTO> enroll(@RequestBody EnrollmentDTO dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();
        Course course = courseRepository.findById(dto.getCourseId()).orElseThrow();

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setProgress(0);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return new ResponseEntity<>(modelMapper.map(saved, EnrollmentDTO.class), HttpStatus.CREATED);
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByUser(@PathVariable Long userId) {
        List<EnrollmentDTO> dtos = enrollmentRepository.findByUserId(userId).stream()
                .map(e -> modelMapper.map(e, EnrollmentDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/progress/{userId}/{courseId}/{itemId}")
    public ResponseEntity<EnrollmentDTO> markItemComplete(@PathVariable Long userId, @PathVariable Long courseId, @PathVariable Long itemId) {
        Enrollment e = enrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElseThrow();
        if(!e.getCompletedItems().contains(itemId)) {
            e.getCompletedItems().add(itemId);
            int totalContent = e.getCourse() != null && e.getCourse().getContent() != null ? e.getCourse().getContent().size() : 1;
            e.setProgress((int) Math.round((double) e.getCompletedItems().size() / totalContent * 100));
            e = enrollmentRepository.save(e);
        }
        return ResponseEntity.ok(modelMapper.map(e, EnrollmentDTO.class));
    }
}
