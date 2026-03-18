package io.github.j_yuhanwang.food_ordering_app.auth_users.mapper;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.role.dtos.RoleDTO;
import org.springframework.stereotype.Component;

/**
 * @author YuhanWang
 * @Date 18/03/2026 1:44 pm
 */
@Component
public class UserMapper {
    public UserDTO toDTO(User user){
        if(user == null){
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .profileUrl(user.getProfileUrl())
                .isActive(user.getUserStatus()==UserStatus.ACTIVE)
                .roles(user.getRoles().stream().map(
                        role->RoleDTO.builder().name(role.getName()).build()
                ).toList())
                .build();
    }
}
