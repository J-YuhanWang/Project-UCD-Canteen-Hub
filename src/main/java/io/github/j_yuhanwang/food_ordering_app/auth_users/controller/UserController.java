package io.github.j_yuhanwang.food_ordering_app.auth_users.controller;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.ChangePasswordRequest;
import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.services.UserService;
import io.github.j_yuhanwang.food_ordering_app.enums.RoleType;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 18/03/2026 8:36 pm
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    //1. Self-service API
    @GetMapping("/me")
    public Response<UserDTO> getMyProfile(){
        log.info("API request: Get current user profile");
        return Response.ok(userService.getOwnAccountDetails());
    }

    @PutMapping("/me")
    public Response<UserDTO> updateMyProfile(@Valid @RequestBody UserDTO userDTO){
        log.info("API request: Update current user profile");
        return Response.ok(userService.updateOwnAccount(userDTO));
    }

    @DeleteMapping("/me")
    public Response<String> deactivateMyAccount(){
        log.info("API request: Deactivate account");
        userService.deactivateOwnAccount();
        return Response.ok("Account deactivated successfully. Please contact support to reactivate.");
    }

    @PostMapping("/me/avatar")
    public Response<UserDTO> uploadMyAvatar(@RequestParam("file") MultipartFile file){
        log.info("API request: Upload avatar");
        return Response.ok(userService.uploadAvatar(file));
    }

    @PutMapping("/me/password")
    public Response<String> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request){
        log.info("API request: Change password");
        userService.changePassword(request);
        return Response.ok("Password updated successfully.");
    }

    //2. Admin-only API
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Response<List<UserDTO>> getAllUsers(){
        log.info("API request: Admin fetching all users");
        return Response.ok(userService.getAllUsers());
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status
            ){
        log.info("API request: Admin updating user {} status to {}",userId,status);
        userService.updateUserStatus(userId,status);
        return Response.ok("User status updated successfully.");
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<String> updateUserRole(
            @PathVariable Long userId,
            @RequestParam RoleType role
            ){
        log.info("API request: Admin updating user {} role to {}",userId,role);
        userService.updateUserRole(userId,role);
        return Response.ok("User role updated successfully.");
    }
}
