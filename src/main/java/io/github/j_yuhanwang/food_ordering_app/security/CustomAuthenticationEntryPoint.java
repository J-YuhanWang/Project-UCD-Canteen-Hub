package io.github.j_yuhanwang.food_ordering_app.security;/*
 * @author BlairWang
 * @Date 22/12/2025 7:07 pm
 * @Version 1.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * This class handles unauthorized access attempts.
 * It is triggered when a user tries to access a protected resource without authentication.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Used to convert Java objects into JSON strings
    private final ObjectMapper objectMapper;

    /**
     * Commences the authentication scheme.
     * This method is called when an AuthenticationException is thrown.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        // 1. Construct the uniform error response object
        Response<?> errorResponse = Response.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value()) //401 error
                .message("Forbidden: You don't have permission.")
                .timestamp(LocalDateTime.now())
                .build();

        // 2. Set response headers and status code
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);//"application/json"
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // 3. Write the JSON string to the response body
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
