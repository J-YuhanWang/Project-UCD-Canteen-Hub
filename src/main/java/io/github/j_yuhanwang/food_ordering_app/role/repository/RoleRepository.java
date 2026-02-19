package io.github.j_yuhanwang.food_ordering_app.role.repository;

import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 *
 * @author YuhanWang
 * @Date 28/01/2026 6:16 pm
 */
public interface RoleRepository extends JpaRepository<Role,Long> {
    /**
     * Retrieves a role by its name (e.g., "ROLE_CUSTOMER").
     * Usage:
     * Used during user registration to assign default roles.
     * Also used by Spring Security to load authorities.
     *
     * @param name The name of the role.
     * @return Optional containing the Role object if found.
     */
    Optional<Role> findByName(String name);
}
