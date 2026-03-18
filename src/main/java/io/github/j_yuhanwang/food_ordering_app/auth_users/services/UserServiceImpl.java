package io.github.j_yuhanwang.food_ordering_app.auth_users.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.ChangePasswordRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.mapper.UserMapper;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.aws.services.AwsS3Service;
import io.github.j_yuhanwang.food_ordering_app.email_notification.dtos.NotificationDTO;
import io.github.j_yuhanwang.food_ordering_app.email_notification.services.NotificationService;
import io.github.j_yuhanwang.food_ordering_app.enums.RoleType;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
    private final RoleRepository roleRepository;

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
        //fetch the user
        User user = getCurrentLoggedInUser();

        //change the status to inactive
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
    @Transactional
    public UserDTO uploadAvatar(MultipartFile file) {
        log.info("Request to upload new avatar for the current user");
        //fetch the user
        User user = getCurrentLoggedInUser();
        //Delete the old image in cloud if it exists at first, but not throw the exception
        if(StringUtils.hasText(user.getProfileUrl())){
            try{
                String oldUrl = user.getProfileUrl();
                //substring(index + 1): Retrieves the filename after "/".
                String oldKey = "profile/" + oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                awsS3Service.deleteFile(oldKey);
                log.info("Deleted old avatar from S3");
            }catch(Exception e){
                log.error("Failed to delete old avatar from S3, proceeding with upload: {}", e.getMessage());
            }
        }

        //upload new image
        String fileName = UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
        String keyName = "profile/"+fileName;//save inside profile folder
        String newProfileUrl = awsS3Service.uploadFile(keyName,file);

        //update the url data to repository
        user.setProfileUrl(newProfileUrl);
        User savedUser = userRepository.save(user);
        log.info("New avatar uploaded and database updated: {}", newProfileUrl);

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        //fetch the user
        User user = getCurrentLoggedInUser();
        //Exception 1: input old password not match the saved user pwd
        if(!passwordEncoder.matches(request.getCurrentPassword(),user.getPassword())){
            throw new BadRequestException("Current password does not match.");
        }
        //Exception 2: new password is empty
        if(!StringUtils.hasText(request.getNewPassword())){
            throw new BadRequestException("New password cannot be empty.");
        }
        //Exception 3: New password and confirmation do not match.
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new BadRequestException("New password and confirmation do not match.");
        }

        //One-way Hashing
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password successfully updated for user: {}", user.getEmail());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> userList =  userRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        return userList.stream()
                //.map(user->userMapper.toDTO(user))
                .map(userMapper::toDTO)
                .toList();
    }

    //For admin role: Update status (banned or reactivated user)
    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        //fetch the user
        User user = userRepository.findById(userId).orElseThrow(
                ()->new ResourceNotFoundException("User","id",userId)
        );

        user.setUserStatus(status);
        userRepository.save(user);
    }

    //For admin role: Change role (upgrade to manager or downgrade)
    @Override
    @Transactional
    public void updateUserRole(Long userId, RoleType role) {
        //fetch the user
        User user = userRepository.findById(userId).orElseThrow(
                ()->new ResourceNotFoundException("User","id",userId)
        );
        //Find Role by Name
        Role newRole = roleRepository.findByName(role.name()).orElseThrow(
                ()->new ResourceNotFoundException("Role","name",role.name())
        );

        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);
    }
}
