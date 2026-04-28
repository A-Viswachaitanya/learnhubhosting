package com.learnhub.backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.learnhub.backend.dto.CourseDTO;
import com.learnhub.backend.entity.Course;
import com.learnhub.backend.entity.User;
import com.learnhub.backend.repository.CourseRepository;
import com.learnhub.backend.repository.UserRepository;
import com.learnhub.backend.service.CourseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = modelMapper.map(courseDTO, Course.class);

        // If a userId is provided, fetch the actual user and attach it to strictly enforce the ManyToOne relation
        if (courseDTO.getUserId() != null) {
            User instructor = userRepository.findById(courseDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("Instructor User not found with ID: " + courseDTO.getUserId()));
            course.setUser(instructor);
        }

        Course savedCourse = courseRepository.save(course);
        return modelMapper.map(savedCourse, CourseDTO.class);
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return modelMapper.map(course, CourseDTO.class);
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setTitle(courseDTO.getTitle());

        if (courseDTO.getUserId() != null) {
            User instructor = userRepository.findById(courseDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("Instructor User not found"));
            course.setUser(instructor);
        }

        Course updatedCourse = courseRepository.save(course);
        return modelMapper.map(updatedCourse, CourseDTO.class);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with ID " + id);
        }
        courseRepository.deleteById(id);
    }
}
