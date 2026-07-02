package com.innowise.orderservice.client;

import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.model.dto.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class UserServiceClient {

    private final WebClient webClient;
    private final String userServiceUrl;

    public UserServiceClient(WebClient webClient, @Value("${user-service.url}") String userServiceUrl) {
        this.webClient = webClient;
        this.userServiceUrl = userServiceUrl;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(Long userId, String authHeader) {
        try {
            return webClient.get()
                    .uri(userServiceUrl + "/api/users/{id}", userId)
                    .headers(httpHeaders -> {
                        if (authHeader != null && !authHeader.isEmpty()) {
                            httpHeaders.set(HttpHeaders.AUTHORIZATION, authHeader);
                        }
                    })
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

    private UserResponse getUserByIdFallback(Long userId, String authHeader, Throwable throwable) {
        if (throwable instanceof UserNotFoundException) {
            throw (UserNotFoundException) throwable;
        }
        throw new IllegalStateException("User service is unavailable for ID: " + userId, throwable);
    }
}