package io.github.j_yuhanwang.food_ordering_app.auth_users.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginResponse;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.RegistrationRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.mapper.UserMapper;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.AccessDeniedException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.UserAlreadyExistsException;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import io.github.j_yuhanwang.food_ordering_app.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Login: 1 Happy Path + 3 sad path (User Not Found, Wrong Password, Not Active)
 * Register: 1 Happy Path + 2 sad path (User Exists, Role Not Found)
 *
 * @author YuhanWang
 * @Date 16/03/2026 5:34 pm
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role studentRole;
    private User activeUser;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder().id(1L).name("ROLE_STUDENT").build();
        activeUser = User.builder()
                .email("test@example.com")
                .password("encoded_pwd")
                .userStatus(UserStatus.ACTIVE)
                .roles(List.of(studentRole))
                .build();
    }

    // ==========================================
    // 1. Login function test
    // ==========================================
    //1.1 HAPPY PATH
    @Test
    @DisplayName("Login - happy path")
    void login_Success_ShouldReturnLoginResponse(){
        //arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "raw_pwd");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("raw_pwd","encoded_pwd")).thenReturn(true);
        when(jwtUtils.generateToken(anyString())).thenReturn("mock_token");

        //act
        LoginResponse loginResponse = authService.login(loginRequest);

        //assert
        assertNotNull(loginResponse);
        assertEquals("mock_token",loginResponse.getToken());
        assertTrue(loginResponse.getRoles().contains("ROLE_STUDENT"));
        verify(userRepository,times(1)).findByEmail("test@example.com");
    }

    //1.2 Login - SAD PATH - USER NOT FOUND
    @Test
    @DisplayName("Login - sad path - User not found")
    void login_WhenEmailNotExists_ShouldReturnBadRequestException(){
        //arrange
        LoginRequest request = new LoginRequest("notexists@example.com", "raw_pwd");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        //act & assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                ()->authService.login(request));
        assertEquals("Invalid email or password",exception.getMessage());
        //Verify password verification logic has never been triggered
        verify(passwordEncoder,never()).matches(any(),any());
    }

    //1.3 Login - SAD PATH - WRONG PASSWORD
    @Test
    @DisplayName("Login - sad path - Wrong password")
    void login_WhenPwdWrong_ShouldReturnBadRequestException(){
        //arrange
        LoginRequest request = new LoginRequest("test@example.com","wrong_pwd");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong_pwd","encoded_pwd")).thenReturn(false);

        //act && assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                ()->authService.login(request));
        assertEquals("Invalid email or password",exception.getMessage());
        verify(jwtUtils,never()).generateToken(anyString());
    }

    //1.4 Login - SAD PATH - NOT ACTIVE
    @Test
    @DisplayName("Login - sad path - Inactive user")
    void login_UserInactive_ShouldReturnAccessDeniedException(){
        //arrange
        LoginRequest request = new LoginRequest("test@example.com","raw_pwd");
        activeUser.setUserStatus(UserStatus.BANNED);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        //act && assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                ()-> authService.login(request));
        assertEquals("Account is not active. Please contact customer support.",exception.getMessage());
        verify(jwtUtils,never()).generateToken(anyString());
    }


    // ==========================================
    // 2. Register: 1 Happy Path + 2 Sad path (User Exists, Role Not Found)
    // ==========================================
    // 2.1 happy path
    @Test
    @DisplayName("Register - Happy path")
    void register_Success_ShouldReturnUserDTO(){
        //ARRANGE
        RegistrationRequest request = RegistrationRequest.builder()
                .name("Blair")
                .email("blair.test@example.com")
                .password("raw_pwd")
                .phoneNumber("0871234567")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");

        User savedUser = User.builder()
                .id(100L) //mock the id generated by database
                .name(request.getName())
                .email(request.getEmail())
                .userStatus(UserStatus.ACTIVE)
                .roles(List.of(studentRole))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO mockDTO = UserDTO.builder()
                .id(100L)
                .name("Blair")
                .email("blair.test@example.com")
                .isActive(true)
                .build();
        when(userMapper.toDTO(savedUser)).thenReturn(mockDTO);

        //ACT
        UserDTO result = authService.register(request);

        //ASSERT
        assertNotNull(result);
        assertEquals(100L,result.getId());
        assertEquals("Blair",result.getName());
        assertTrue(result.isActive());

        verify(userMapper,times(1)).toDTO(any(User.class));
        verify(userRepository,times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register - sad path - User exists")
    void register_WhenUserExists_ShouldReturnUserAlreadyExistsException(){
        //arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .email("exists@example.com")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        //act & assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                ()->authService.register(request));
        assertEquals("Email already exists.", exception.getMessage());
        verify(roleRepository,never()).findByName(anyString());
        verify(passwordEncoder,never()).encode(anyString());
        verify(userRepository,never()).save(any(User.class));
        verify(userMapper,never()).toDTO(any(User.class));
    }

    @Test
    @DisplayName("Register - sad path - Role not found")
    void register_WhenRoleNotFound_ShouldReturnResourceNotFoundException(){
        //arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .email("new@example.com")
                .roles(List.of("ROLE_BOSS"))
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_BOSS")).thenReturn(Optional.empty());

        //act & assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->authService.register(request));
        assertTrue(exception.getMessage().contains("not found with"));
        verify(roleRepository,times(1)).findByName("ROLE_BOSS");
        verify(userRepository,times(1)).existsByEmail(anyString());
        verify(passwordEncoder,never()).encode(anyString());
        verify(userRepository,never()).save(any(User.class));
        verify(userMapper,never()).toDTO(any(User.class));
    }
}
