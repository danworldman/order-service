package com.innowise.orderservice.client;

import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.model.dto.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient webClient;

    @Value("${user-service.url:http://localhost:8080}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(Long userId) {
        try {
            return webClient.get()
                    .uri(userServiceUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .block();
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new UserNotFoundException("User not found with id: " + userId);
            }
            throw exception;
        }
    }

    @CircuitBreaker(name = "userServiceEmail", fallbackMethod = "getUserByEmailFallback")
    public UserResponse getUserByEmail(String email) {
        try {
            return webClient.get()
                    .uri(userServiceUrl + "/api/users/by-email?email={email}", email)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .block();
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new UserNotFoundException("User not found with email: " + email);
            }
            throw exception;
        }
    }

    private UserResponse getUserByIdFallback(Long userId, Throwable throwable) {
        if (throwable instanceof UserNotFoundException) {
            throw (UserNotFoundException) throwable;
        }
        return new UserResponse(userId, "Unknown", "Unknown", "unknown@service.com");
    }

    private UserResponse getUserByEmailFallback(String email, Throwable throwable) {
        if (throwable instanceof UserNotFoundException) {
            throw (UserNotFoundException) throwable;
        }
        return new UserResponse(0L, "Unknown", "Unknown", email);
    }
}