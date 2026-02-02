package io.github.j_yuhanwang.food_ordering_app.auth_users.dtos;

import lombok.Data;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 02/02/2026 11:38 am
 */
@Data
public class LoginResponse {
    private String token;
    private List<String> roles;
}
