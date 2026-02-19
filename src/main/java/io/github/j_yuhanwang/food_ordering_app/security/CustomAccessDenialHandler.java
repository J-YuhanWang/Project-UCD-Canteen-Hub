package io.github.j_yuhanwang.food_ordering_app.security;/*
 * @author BlairWang
 * @Date 22/12/2025 9:23 pm
 * @Version 1.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom handler for Access Denied (HTTP 403) errors.
 * This is triggered when an authenticated user attempts to access a resource
 * they do not have the required permissions for.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDenialHandler implements AccessDeniedHandler {

    // Inject ObjectMapper to convert the Response object into a JSON string
    private final ObjectMapper objectMapper;


    /**
     * Handles the access denied failure.
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        log.warn("Access Denied for user: {}", request.getUserPrincipal());

        // 1. Construct the uniform error response object
        Response<?> errorResponse = Response.builder()
                .statusCode(HttpStatus.FORBIDDEN.value()) //403 error
                .message("Access Denied: You do not have permission to access this resource.")
                .timestamp(LocalDateTime.now())
                .build();

        // 2. Set response headers and status code
//        response.setContentType("application/json");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //"application/json"
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        // 3. Write the JSON string to the response body
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
