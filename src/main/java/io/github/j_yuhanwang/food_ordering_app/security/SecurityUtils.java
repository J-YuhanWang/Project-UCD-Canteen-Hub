package io.github.j_yuhanwang.food_ordering_app.security;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author YuhanWang
 * @Date 05/04/2026 6:14 pm
 */
public class SecurityUtils {
    //get current user from Security context
    public static String getCurrentUserEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    //obtain authentication information
    public static boolean hasRole(String roleName){
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(grantedAuthority->grantedAuthority.getAuthority().equals(roleName));
    }
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    public static boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }
}
