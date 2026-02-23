package io.github.j_yuhanwang.food_ordering_app.auth_users.repository;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access layer for User entity.
 *
 * @author YuhanWang
 * @Date 28/01/2026 2:18 pm
 */
public interface UserRepository extends JpaRepository<User,Long> {
    /**
     * Retrieves a user by their unique email address.
     * Usage:Primarily used during the Login process to fetch user credentials for authentication.
     *
     * @param email The email address to search for.
     * @return an {@link Optional} containing the User if found, or empty if no such user exists.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user already exists with the given email.
     * Usage: Crucial for the Registration process to prevent duplicate accounts
     * with the same email address.
     *
     * @param email The email address to search for.
     * @return true if an account already exists with this email, false otherwise.
     */
    boolean existsByEmail(String email);

    long countByRolesContaining(Role role);
}
