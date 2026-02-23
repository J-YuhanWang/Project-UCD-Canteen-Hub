package io.github.j_yuhanwang.food_ordering_app.role.controller;

import io.github.j_yuhanwang.food_ordering_app.response.Response;
import io.github.j_yuhanwang.food_ordering_app.role.dtos.RoleDTO;
import io.github.j_yuhanwang.food_ordering_app.role.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 23/02/2026 3:30 pm
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public Response<RoleDTO> createRole(@RequestBody @Valid RoleDTO roleDTO){
        RoleDTO data = roleService.createRole(roleDTO);
        return Response.ok(data);
    }

    @PutMapping
    public Response<RoleDTO> updateRole(@RequestBody @Valid RoleDTO roleDTO){
        RoleDTO data = roleService.updateRole(roleDTO);
        return Response.ok(data);
    }

    @GetMapping
    public Response<List<RoleDTO>> getAllRoles(){
        List<RoleDTO> roles = roleService.getAllRoles();
        return Response.ok(roles);
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteRole(@PathVariable Long id){
        roleService.deleteRole(id);
        return Response.ok("Role delete successfully.");
    }


}
