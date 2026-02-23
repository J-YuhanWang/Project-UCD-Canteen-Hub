package io.github.j_yuhanwang.food_ordering_app.role.services;

import io.github.j_yuhanwang.food_ordering_app.response.Response;
import io.github.j_yuhanwang.food_ordering_app.role.dtos.RoleDTO;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 23/02/2026 1:22 pm
 */
public interface RoleService {
//    CRUD
    RoleDTO createRole(RoleDTO roleDTO);
    RoleDTO updateRole(RoleDTO roleDTO);
    List<RoleDTO> getAllRoles();
    void deleteRole(Long id);
}
