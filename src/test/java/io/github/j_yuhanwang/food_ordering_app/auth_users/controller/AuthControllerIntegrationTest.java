package io.github.j_yuhanwang.food_ordering_app.auth_users.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.RegistrationRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for AuthController.
 * HTTP Request -> Controller (@Valid) -> Service -> DB -> HTTP Response
 *
 * @author YuhanWang
 * @Date 16/03/2026 8:15 pm
 */

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(){
        if(!roleRepository.existsByName("ROLE_STUDENT")){
            roleRepository.save(Role.builder().name("ROLE_STUDENT").build());
        }
    }

    // 1. Login - happy path
    @Test
    @DisplayName("Integration: Login - Happy Path")
    void login_Success() throws Exception {
        //arrange
        Role studentRole = roleRepository.findByName("ROLE_STUDENT").get();
        User dbUser = User.builder()
                .name("LoginTestUser")
                .email("test@example.com")
                .password(passwordEncoder.encode("secret_pwd"))
                .roles(List.of(studentRole))
                .userStatus(UserStatus.ACTIVE)
                .build();
        userRepository.save(dbUser);

        //Mock a login request
        LoginRequest request = new LoginRequest("test@example.com","secret_pwd");

        //act && assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Login successfully"));
    }

    // 2. Register - happy path
    @Test
    @DisplayName("Integration: Register - Happy path")
    void register_Success() throws Exception {
        //arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .name("RegisterUser")
                .email("register@example.com")
                .password("secret123")
                .address("UCD campus")
                .phoneNumber("0871234567")//without roles and status setting
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))) //convert request to json
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.name").value("RegisterUser"))
                .andExpect(jsonPath("$.data.email").value("register@example.com"))
                .andExpect(jsonPath("$.data.roles[0].name").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.data.active").value(true));
        assertTrue(userRepository.existsByEmail("register@example.com"));
    }

    // 3.Register - sad path - Valid
    @Test
    @DisplayName("Integration: Register - Sad Path - Invalid Email (400)")
    void register_InvalidEmail_ShouldReturn400() throws Exception {
        //arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .name("InvalidEmailTest")
                .email("is-not-an-email")
                .password("test123")
                .phoneNumber("0871234567")
                .address("UCD campus")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // BadRequest = 400
    }
}
