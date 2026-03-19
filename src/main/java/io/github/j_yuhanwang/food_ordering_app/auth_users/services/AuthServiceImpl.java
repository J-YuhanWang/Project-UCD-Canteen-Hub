package io.github.j_yuhanwang.food_ordering_app.auth_users.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginResponse;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.RegistrationRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.mapper.UserMapper;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.UserAlreadyExistsException;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import io.github.j_yuhanwang.food_ordering_app.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for user authentication and registration.
 *
 * @author YuhanWang
 * @Date 16/03/2026 11:41 am
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    //传入的账号与密码封装为authentication对象，将传入的密码通过passwordEncoder加密，与数据库中userrepository用户的密码作对比，如果通过了返回一个jwtUtils生成的string
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserDTO register(RegistrationRequest registrationRequest) {

        log.info("INSIDE register()");
        // 1. Check if email is already registered
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // 2. Resolve user roles
        List<Role> userRoles;
        if (registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()) {
            // 2.1 Validate and assign provided roles
            userRoles = registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName)))
                    .toList();
        } else {
            // 2.2 Assign default role if none provided
            // TODO: [Refactor] Convert List<Role> roles to List<RoleType> enum after MVP login flow is stable.
            Role defaultRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role", "name", "ROLE_STUDENT"));
            userRoles = List.of(defaultRole);
        }

        // 3. Encode password and build User entity
        String encodedPwd = passwordEncoder.encode(registrationRequest.getPassword());
        User newUser = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .address(registrationRequest.getAddress())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .password(encodedPwd)
                .roles(userRoles)
                .build();

        // 4. Save user to database
        User savedUser = userRepository.save(newUser);
        // 5. Return sanitized UserDTO (excluding sensitive data like password)
        log.info("User registered successfully.");
        return userMapper.toDTO(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. Fetch user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        // 2. Verify password match
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        // 3. Check if account is active
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("Account is not active. Please contact customer support.");
        }
        // 4. Generate JWT token
        String token = jwtUtils.generateToken(user.getEmail());

        // 5. Extract roles and build response
        List<String> roles = user.getRoles().stream()
//                .map(role-> role.getName())
                .map(Role::getName)
                .toList();

        return LoginResponse.builder()
                .token(token)
                .roles(roles)
                .build();
    }
}
