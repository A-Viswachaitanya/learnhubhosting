package com.learnhub.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnhub.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.learnhub.backend.service.RecaptchaService recaptchaService;
    private final com.learnhub.backend.repository.UserRepository userRepository;

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("recaptchaToken");
            if (!recaptchaService.verifyCaptcha(token)) {
                return ResponseEntity.badRequest().body("reCAPTCHA validation failed.");
            }

            String email = request.get("email");
            String name = request.get("name");
            System.out.println("[Backend] Attempting to send OTP to: " + email);
            authService.sendOtp(email, name);
            return ResponseEntity.ok("OTP sent to " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Mail Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Boolean> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        boolean isValid = authService.verifyOtp(email, otp);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String token = request.get("recaptchaToken");
        if (!recaptchaService.verifyCaptcha(token)) {
            return ResponseEntity.badRequest().body("reCAPTCHA validation failed.");
        }

        String email = request.get("email");
        String password = request.get("password");

        com.learnhub.backend.entity.User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(401).body("Invalid credentials.");
        }

        return ResponseEntity.ok(user);
    }
}
