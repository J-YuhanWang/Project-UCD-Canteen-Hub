package io.github.j_yuhanwang.food_ordering_app.auth_users.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.ChangePasswordRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.RoleType;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 18/03/2026 11:15 am
 */
public interface UserService {
    //1.Internal helper - extract user from jwt token
    User getCurrentLoggedInUser();

    //---------------------------------------
    //2. User profile - for all users/roles
    //2.1 get account details
    UserDTO getOwnAccountDetails();

    //2.2 update account details
    UserDTO updateOwnAccount(UserDTO userDTO);

    //2.3 deactivate account(INACTIVE/ACTIVE)
    void deactivateOwnAccount();

    //2.4 upload user avatar(AWS S3)
    UserDTO uploadAvatar(MultipartFile file);

    //2.5 change password
    void changePassword(ChangePasswordRequest request);

    //---------------------------------------
    //3. Admin only (for ROLE_ADMIN)
    List<UserDTO> getAllUsers();

    //3.2 change user status, BANNED/ACTIVE
    void updateUserStatus(Long userId, UserStatus status);

    //3.3 change user role
    void updateUserRole(Long userId, RoleType role);
}
