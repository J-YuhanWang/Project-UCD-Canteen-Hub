package io.github.j_yuhanwang.food_ordering_app.role.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.role.dtos.RoleDTO;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Role management.
 * Adheres to Clean Architecture by strictly handling domain entities and returning pure DTOs.
 * @author YuhanWang
 * @Date 23/02/2026 1:30 pm
 */

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class RoleServiceImpl implements RoleService{
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        log.info("Attempting to create new role: {}",roleDTO.getName());

        // 1. Fail-fast validation: Prevent duplicate roles at the business level
        if(roleRepository.existsByName(roleDTO.getName())){
            throw new BadRequestException("Role with name '" + roleDTO.getName() + "' already exists.");
        }

        //2. Conversion and Storage
        // Receiving the roleDTO from the front end, scanning and mapping it to a Role class object using modelMapper
        Role role = modelMapper.map(roleDTO,Role.class);
        // Storing the converted object in repo, obtaining a complete object savedRole with an auto-incrementing ID
        Role savedRole = roleRepository.save(role);

        // 3. Return pure DTO to decouple business logic from HTTP response wrappers
        return modelMapper.map(savedRole,RoleDTO.class);
    }

    @Override
    public RoleDTO updateRole(RoleDTO roleDTO) {
        log.info("Attempting to update role ID: {}", roleDTO.getId());

        // 1. Fetch existing entity or throw exception if not found
        Role existingRole = roleRepository.findById(roleDTO.getId())
                .orElseThrow(()->new ResourceNotFoundException("Role","id",roleDTO.getId()));

        // 2. Prevent renaming to an already existing role name (excluding itself)
        if(!existingRole.getName().equals(roleDTO.getName()) &&roleRepository.existsByName(roleDTO.getName())){
            throw new BadRequestException("Role with name already exists.");
        }
        // 3. Update fields and persist
        existingRole.setName(roleDTO.getName());
        Role updatedRole = roleRepository.save(existingRole);

        return modelMapper.map(updatedRole, RoleDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles from database");

        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role->(modelMapper.map(role, RoleDTO.class)))
                .toList();
    }

    @Override
    public void deleteRole(Long id) {
        log.info("Attempting to delete role ID: {}", id);
        // 1. Verify existence
        Role role = roleRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Role","id",id));

        // 2. Cross-table validation: Prevent deletion if role is actively assigned to users
        if(userRepository.countByRolesContaining(role) >0){
            throw new BadRequestException("Role is currently in use and cannot be deleted.");
        }
        // 3. Safe deletion using the fetched entity
        roleRepository.delete(role);
    }
}
