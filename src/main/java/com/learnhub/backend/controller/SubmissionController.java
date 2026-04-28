package com.learnhub.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnhub.backend.dto.SubmissionDTO;
import com.learnhub.backend.entity.CourseContent;
import com.learnhub.backend.entity.Submission;
import com.learnhub.backend.entity.User;
import com.learnhub.backend.repository.CourseContentRepository;
import com.learnhub.backend.repository.SubmissionRepository;
import com.learnhub.backend.repository.UserRepository;
import com.learnhub.backend.service.MailService;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CourseContentRepository contentRepository;
    private final ModelMapper modelMapper;
    private final MailService mailService;

    @PostMapping
    public ResponseEntity<SubmissionDTO> submitAssignment(@RequestBody SubmissionDTO dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();
        CourseContent assignment = contentRepository.findById(dto.getAssignmentId()).orElseThrow();

        Submission sub = new Submission();
        sub.setUser(user);
        sub.setAssignment(assignment);
        sub.setContent(dto.getContent());

        Submission saved = submissionRepository.save(sub);

        // Send submission confirmation email to student
        String studentEmail = user.getEmail();
        String studentName = user.getName();
        String assignmentTitle = assignment.getTitle();
        String courseTitle = assignment.getCourse() != null ? assignment.getCourse().getTitle() : "your course";

        String subject = "Assignment Submitted Successfully - LearnHub";
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e5e7eb; border-radius: 10px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'>" +
                "<div style='text-align: center; padding-bottom: 20px; border-bottom: 1px solid #e5e7eb;'>" +
                "<h2 style='color: #4F46E5; margin: 0;'>✅ Submission Confirmed</h2>" +
                "</div>" +
                "<div style='padding: 20px 0;'>" +
                "<p style='font-size: 16px; color: #374151;'>Hello <b>" + studentName + "</b>,</p>" +
                "<p style='font-size: 16px; color: #374151;'>We have successfully received your assignment submission for <strong>\"" + assignmentTitle + "\"</strong> in the course <strong>\"" + courseTitle + "\"</strong>.</p>" +
                "<div style='background: #F9FAFB; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #10B981;'>" +
                "<p style='margin: 0; font-size: 15px; color: #4B5563;'>Your instructor will review your work and provide grading and feedback soon.</p>" +
                "</div>" +
                "<p style='font-size: 15px; color: #4B5563;'>You can log in to LearnHub at any time to review your submission status.</p>" +
                "</div>" +
                "<div style='padding-top: 20px; border-top: 1px solid #e5e7eb; text-align: center;'>" +
                "<p style='font-size: 14px; color: #9CA3AF; margin: 0;'>Keep up the great work!<br><strong>LearnHub Team</strong></p>" +
                "</div>" +
                "</div>";

        new Thread(() -> {
            try {
                mailService.sendMail(studentEmail, subject, body);
                System.out.println("[Mail] Submission confirmation sent to " + studentEmail);
            } catch (Exception e) {
                System.out.println("[Mail] Failed to send submission confirmation: " + e.getMessage());
            }
        }).start();

        return ResponseEntity.ok(modelMapper.map(saved, SubmissionDTO.class));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubmissionDTO>> getSubmissionsByUser(@PathVariable Long userId) {
        List<SubmissionDTO> dtos = submissionRepository.findByUserId(userId).stream()
                .map(s -> modelMapper.map(s, SubmissionDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<SubmissionDTO>> getSubmissionsByCourse(@PathVariable Long courseId) {
        List<SubmissionDTO> dtos = submissionRepository.findByAssignmentCourseId(courseId).stream()
                .map(s -> modelMapper.map(s, SubmissionDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/grade")
    public ResponseEntity<SubmissionDTO> gradeSubmission(@PathVariable Long id, @RequestBody SubmissionDTO dto) {
        Submission sub = submissionRepository.findById(id).orElseThrow();
        sub.setGrade(dto.getGrade());
        sub.setFeedback(dto.getFeedback());
        Submission saved = submissionRepository.save(sub);

        // Send grade notification email to student
        User student = saved.getUser();
        String assignmentTitle = saved.getAssignment().getTitle();
        String studentEmail = student.getEmail();
        String studentName = student.getName();
        String grade = dto.getGrade();
        String feedback = dto.getFeedback() != null ? dto.getFeedback() : "No feedback provided.";

        String subject = "Your assignment has been graded - LearnHub";
        String gradeColor = "#4F46E5";
        try {
            if (Double.parseDouble(grade) >= 50) {
				gradeColor = "#10B981";
			} else {
				gradeColor = "#EF4444";
			}
        } catch(Exception ignored) {}

        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e5e7eb; border-radius: 10px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'>" +
                "<div style='text-align: center; padding-bottom: 20px; border-bottom: 1px solid #e5e7eb;'>" +
                "<h2 style='color: #4F46E5; margin: 0;'>📝 Assignment Graded</h2>" +
                "</div>" +
                "<div style='padding: 20px 0;'>" +
                "<p style='font-size: 16px; color: #374151;'>Hello <b>" + studentName + "</b>,</p>" +
                "<p style='font-size: 16px; color: #374151;'>Your submission for <strong>\"" + assignmentTitle + "\"</strong> has been reviewed.</p>" +
                "<div style='background: #F9FAFB; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid " + gradeColor + ";'>" +
                "<p style='margin: 0 0 10px 0; font-size: 16px; color: #374151;'><strong>Grade:</strong> <span style='font-size: 18px; font-weight: bold; color: " + gradeColor + ";'>" + grade + " / 100</span></p>" +
                "<p style='margin: 0; font-size: 15px; color: #4B5563;'><strong>Feedback:</strong> <br><br><em>\"" + feedback + "\"</em></p>" +
                "</div>" +
                "<p style='font-size: 15px; color: #4B5563;'>Log in to LearnHub to view the full details of your ongoing progress.</p>" +
                "</div>" +
                "<div style='padding-top: 20px; border-top: 1px solid #e5e7eb; text-align: center;'>" +
                "<p style='font-size: 14px; color: #9CA3AF; margin: 0;'>Thanks,<br><strong>LearnHub Team</strong></p>" +
                "</div>" +
                "</div>";

        new Thread(() -> {
            try {
                mailService.sendMail(studentEmail, subject, body);
                System.out.println("[Mail] Grade notification sent to " + studentEmail);
            } catch (Exception e) {
                System.out.println("[Mail] Failed to send grade notification: " + e.getMessage());
            }
        }).start();

        return ResponseEntity.ok(modelMapper.map(saved, SubmissionDTO.class));
    }
}

