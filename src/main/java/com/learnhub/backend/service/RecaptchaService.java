package com.learnhub.backend.service;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {
    private static final String RECAPTCHA_SECRET = "6Lfo8KIsAAAAAB4XEiMkbAbUC8k6_nQF92LKkOr9";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyCaptcha(String token) {
        if (token == null || token.isEmpty()) {
			return false;
		}
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("secret", RECAPTCHA_SECRET);
            map.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, request, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) {
				return false;
			}
            return (Boolean) body.getOrDefault("success", false);
        } catch (Exception e) {
            System.err.println("Recaptcha verification failed: " + e.getMessage());
            return false;
        }
    }
}
