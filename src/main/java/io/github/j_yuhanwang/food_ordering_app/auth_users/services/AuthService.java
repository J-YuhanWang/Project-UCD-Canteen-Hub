package io.github.j_yuhanwang.food_ordering_app.auth_users.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.LoginResponse;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.RegistrationRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;

/**
 * @author YuhanWang
 * @Date 16/03/2026 9:36 am
 */
public interface AuthService {
    //At mvp stage can just return token(string), later modify to LoginResponseDTO
    LoginResponse login(LoginRequest loginRequest);
    //registration can return a UserDTO
    UserDTO register(RegistrationRequest registrationRequest);
}
