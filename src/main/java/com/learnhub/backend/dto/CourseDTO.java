package com.learnhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String category;

    private Long userId;
    private java.util.List<CourseContentDTO> content;
}
