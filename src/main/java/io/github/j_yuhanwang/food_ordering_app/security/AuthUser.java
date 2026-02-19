package io.github.j_yuhanwang.food_ordering_app.security;/*
 * @author BlairWang
 * @Date 23/12/2025 10:06 am
 * @Version 1.0
 */

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


/**
 * Adapter class that wraps the domain User entity to be compatible with Spring Security.
 * It implements UserDetails to provide identity and authorization information to the framework.
 */
@Builder
@Data
public class AuthUser implements UserDetails {

    /**
     * Data resource : The core domain User entity retrieved from the database.
     */
    private User user;

    /**
     * Converts the user's roles from the database into a collection of GrantedAuthority.
     * Spring Security uses these authorities to control access to different API endpoints.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(user.getRoles()==null){
            return Collections.emptyList();
        }
        return user.getRoles().stream()
                // Map each business role to a SimpleGrantedAuthority that the framework understands
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    /**
     * Returns the encrypted password from the User entity for authentication.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Uses the email as the primary identification (username) for the login process.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Checks if the account is active based on the business logic in the User entity.
     * If returns false, authentication will be rejected even with the correct password.
     */
    @Override
    public boolean isEnabled() {
        return user.getUserStatus() == UserStatus.ACTIVE;
    }
}
