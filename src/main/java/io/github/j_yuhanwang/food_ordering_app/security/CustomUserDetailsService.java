package io.github.j_yuhanwang.food_ordering_app.security;/*
 * @author BlairWang
 * @Date 23/12/2025 12:03 pm
 * @Version 1.0
 */

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Custom implementation of UserDetailsService to integrate with the database.
 * This service is used by Spring Security to load user-specific data during authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the email provided during login.
     * @param username The email entered by the user.
     * @return A fully populated AuthUser principal.
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Fetch user from DB, throw custom exception if not found
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Wrap the User entity into AuthUser (UserDetails) for Spring Security
        return AuthUser.builder()
                .user(user) //AuthUser authUser = new AuthUser(); authUser.setUser(user);
                .build();
    }
}
