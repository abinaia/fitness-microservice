package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;                          // ✅ Spring's HttpStatus
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) throws RuntimeException {
        log.info("Calling userValidation API for user Id: {}", userId);
        try {
            return Boolean.TRUE.equals(userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {   // ✅ .equals(), Spring HttpStatus
                throw new RuntimeException("User not found: " + userId);
            } else if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) { // ✅ .equals(), Spring HttpStatus
                throw new RuntimeException("Invalid Request");
            }
            throw new RuntimeException("Unexpected error: " + e.getMessage()); // ✅ fallback return path
        }
    }
}