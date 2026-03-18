package io.github.j_yuhanwang.food_ordering_app.auth_users.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.ChangePasswordRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.mapper.UserMapper;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.aws.services.AwsS3Service;
import io.github.j_yuhanwang.food_ordering_app.email_notification.dtos.NotificationDTO;
import io.github.j_yuhanwang.food_ordering_app.email_notification.services.NotificationService;
import io.github.j_yuhanwang.food_ordering_app.enums.NotificationType;
import io.github.j_yuhanwang.food_ordering_app.enums.RoleType;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.role.dtos.RoleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 18/03/2026 12:31 pm
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final AwsS3Service awsS3Service;

    //Internal helper
    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User","email name",email));
    }

    @Override
    public UserDTO getOwnAccountDetails() {
        User user = getCurrentLoggedInUser();
        return userMapper.toDTO(user);
    }

    //not sensitive fields: name, phoneNumber, address
    @Override
    public UserDTO updateOwnAccount(UserDTO userDTO) {
        log.info("Updating basic profile details for current user");
        User user = getCurrentLoggedInUser();
        //StringUtils == isNotNull, !isEmpty(), NotBlank
        if(StringUtils.hasText(userDTO.getName())){
            user.setName(userDTO.getName());
        }
        if(StringUtils.hasText(userDTO.getPhoneNumber())){
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if(StringUtils.hasText(userDTO.getAddress())){
            user.setAddress(userDTO.getAddress());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    /**
     * After a user voluntarily deactivates their account, they receive an email notification.
     */
    @Override
    @Transactional
    public void deactivateOwnAccount() {
        log.info("Request to deactivate account for current logged-in user");

        User user = getCurrentLoggedInUser();
        user.setUserStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        //Send the notification email
        log.info("User status updated to DEACTIVATED for email: {}", user.getEmail());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .subject("Account Deactivated")
                .recipient(user.getEmail())
                .body("Your account has been deactivated successfully. If this was a mistake, please contact our support team.")
                .build();
        notificationService.sendVerificationEmail(notificationDTO);

    }

    @Override
    public UserDTO uploadAvatar(MultipartFile file) {
        return null;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

    @Override
    public List<UserDTO> getAllUsers() {
        return List.of();
    }

    @Override
    public void updateUserStatus(Long userId, UserStatus status) {

    }

    @Override
    public void updateUserRole(Long userId, RoleType role) {

    }
}
