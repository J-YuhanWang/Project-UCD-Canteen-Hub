package io.github.j_yuhanwang.food_ordering_app.auth_users.controller;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginResponse;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.RegistrationRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.services.AuthService;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YuhanWang
 * @Date 16/03/2026 3:48 pm
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Response<UserDTO> register(@RequestBody @Valid RegistrationRequest registrationRequest){
        UserDTO userDTO = authService.register(registrationRequest);
        return Response.ok(userDTO,"User registered successfully");
    }

    @PostMapping("/login")
    public Response<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest){
        LoginResponse loginResponse = authService.login(loginRequest);
        return Response.ok(loginResponse,"Login successfully");
    }
}
