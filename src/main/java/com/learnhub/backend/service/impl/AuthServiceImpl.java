package com.learnhub.backend.service.impl;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.learnhub.backend.service.AuthService;
import com.learnhub.backend.service.MailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MailService mailService;
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    @Override
    public void sendOtp(String email, String name) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        otpStorage.put(email, otp);

        System.out.println("========================================");
        System.out.println("[OTP] Code for " + email + " : " + otp);
        System.out.println("========================================");

        String subject = "Verify your LearnHub Account";
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e5e7eb; border-radius: 10px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'>" +
                "<div style='text-align: center; padding-bottom: 20px; border-bottom: 1px solid #e5e7eb;'>" +
                "<h2 style='color: #4F46E5; margin: 0;'>👋 Welcome to LearnHub!</h2>" +
                "</div>" +
                "<div style='padding: 20px 0;'>" +
                "<p style='font-size: 16px; color: #374151;'>Hello <b>" + name + "</b>,</p>" +
                "<p style='font-size: 16px; color: #374151;'>Your One-Time Password (OTP) for registration is:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<span style='font-size: 32px; font-weight: bold; background: #EEF2FF; color: #4F46E5; padding: 15px 30px; border-radius: 8px; letter-spacing: 5px; box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.06);'>" + otp + "</span>" +
                "</div>" +
                "<p style='font-size: 15px; color: #4B5563;'>Please use this code to complete verifying your account. Valid for the next 10 minutes.</p>" +
                "</div>" +
                "<div style='padding-top: 20px; border-top: 1px solid #e5e7eb; text-align: center;'>" +
                "<p style='font-size: 14px; color: #9CA3AF; margin: 0;'>Thanks,<br><strong>LearnHub Team</strong></p>" +
                "</div>" +
                "</div>";


        new Thread(() -> mailService.sendMail(email, subject, body)).start();
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }
}
