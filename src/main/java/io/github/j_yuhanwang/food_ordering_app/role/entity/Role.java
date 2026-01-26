package io.github.j_yuhanwang.food_ordering_app.role.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a security role or authority within the application.
 * <p>
 * This entity implements Role-Based Access Control (RBAC). It defines specific
 * permissions assigned to users (e.g., accessing admin panels, placing orders).
 * Common examples include "ROLE_CUSTOMER", "ROLE_ADMIN", etc.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:39 am
 */
@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique name of the role.
     * <p>
     * By convention in Spring Security, roles should typically start with the "ROLE_" prefix
     * (e.g., "ROLE_ADMIN", "ROLE_CUSTOMER").
     * Must be unique to avoid permission conflicts.
     */
    @Column(unique = true,nullable = false)
    private String name;
}
