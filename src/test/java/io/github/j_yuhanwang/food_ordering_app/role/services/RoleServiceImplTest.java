package io.github.j_yuhanwang.food_ordering_app.role.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.role.dtos.RoleDTO;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author YuhanWang
 * @Date 24/02/2026 11:05 am
 */

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private RoleDTO roleDTO;

//    configuration: Prepare standard data before each test run.
    @BeforeEach
    void setUp(){
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_STUDENT");

        roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setName("ROLE_STUDENT");
    }

    // ==========================================
    // 1. Create Role Tests
    // ==========================================
    @Test
    @DisplayName("Create Role - happy path")
    void createRole_Success_ShouldReturnRoleDTO(){
        //Arrange
        when(roleRepository.existsByName(roleDTO.getName())).thenReturn(false);
        when(modelMapper.map(roleDTO,Role.class)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(role);
        when(modelMapper.map(role, RoleDTO.class)).thenReturn(roleDTO);

        //act
        RoleDTO result = roleService.createRole(roleDTO);

        //assert
        //1.not null
        assertNotNull(result);
        //2.roleDTO's name are equal
        assertEquals("ROLE_STUDENT",result.getName());
        //3.the
        verify(roleRepository,times(1)).save(role);
    }

    @Test
    @DisplayName("Create Role - Sad Path: Duplicate Name Throws BadRequestException")
    void createRole_DuplicateName_ShouldThrowBadRequestException(){
        //arrange
        when(roleRepository.existsByName(roleDTO.getName())).thenReturn(true);

        //act
        BadRequestException exception = assertThrows(BadRequestException.class,()->
                roleService.createRole(roleDTO));
        //assert
        assertTrue(exception.getMessage().contains("already exists"));
        verify(roleRepository,never()).save(any(Role.class));
    }

    // ==========================================
    // 2. Update Role Tests
    // ==========================================

    @Test
    @DisplayName("Update Role - Happy Path")
    void updateRole_Success_ShouldReturnRoleDTO(){
        //arrange
        RoleDTO updateReq = new RoleDTO();
        updateReq.setId(1L);
        updateReq.setName("ROLE_ADMIN");//A new name

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(false);
        when(roleRepository.save(role)).thenReturn(role);
        when(modelMapper.map(role, RoleDTO.class)).thenReturn(updateReq);

        //act
        RoleDTO result = roleService.updateRole(updateReq);

        //assert
        assertNotNull(result);
        assertEquals("ROLE_ADMIN",role.getName());
        assertEquals("ROLE_ADMIN",result.getName());
        verify(roleRepository,times(1)).save(role);
    }

    @Test
    @DisplayName("Update Role - Sad Path: Role id is not found")
    void updateRole_WhenIdNotFound_ShouldThrowResourceNotFoundException(){
        //arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        //act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,()->roleService.updateRole(roleDTO));
        //assert
        assertTrue(exception.getMessage().contains("not found with"));
        verify(roleRepository,never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Update Role - Sad Path: New name already exists")
    void updateRole_WhenNameExists_ShouldThrowBadRequestException(){
        //arrange
        RoleDTO updateReq = new RoleDTO();
        updateReq.setId(1L);
        updateReq.setName("ROLE_EXISTING");// An existing name

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName("ROLE_EXISTING")).thenReturn(true);

        //act
        BadRequestException exception = assertThrows(BadRequestException.class,()->roleService.updateRole(updateReq));
        //assert
        assertEquals("Role with name already exists.",exception.getMessage());
        verify(roleRepository,never()).save(any(Role.class));
    }

    // ==========================================
    // 3. Get All Roles Tests
    // ==========================================
    @Test
    @DisplayName("Get All Roles - Happy path")
    void getAllRoles_Success_ShouldReturnListOfRole(){
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(modelMapper.map(role, RoleDTO.class)).thenReturn(roleDTO);

        List<RoleDTO> result = roleService.getAllRoles();

        //Assert
        assertEquals(1,result.size());
        assertEquals("ROLE_STUDENT",result.getFirst().getName());
        verify(roleRepository,times(1)).findAll();

    }

    @Test
    @DisplayName("Get All Roles - Success: No roles found")
    void getAllRoles_WhenNoRoles_ShouldReturnEmptyList(){
        //arrange
        when(roleRepository.findAll()).thenReturn(List.of());
        //act
        List<RoleDTO> result = roleService.getAllRoles();
        //Assert -  whether it will return empty list instead of null
        assertEquals(0,result.size());
        assertTrue(result.isEmpty());
        verify(modelMapper,never()).map(any(),any());
    }

    // ==========================================
    // 4. Delete Role Tests
    // ==========================================
    @Test
    @DisplayName("Delete Role - Happy Path: No users linked")
    void deleteRole_Success(){
        //arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.countByRolesContaining(role)).thenReturn(0L);

        //act
        roleService.deleteRole(1L);
        //assert
        verify(roleRepository,times(1)).delete(role);
    }

    @Test
    @DisplayName("Delete Role - Sad Path: Role id is not found")
    void deleteRole_WhenIdNotFound_ShouldReturnResourceNotFoundException(){
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        //act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->roleService.deleteRole(1L));
        //assert
        assertTrue(exception.getMessage().contains("not found with"));
        verify(roleRepository,never()).delete(any(Role.class));
    }

    @Test
    @DisplayName("Delete Role - Sad Path: Role in use by users")
    void deleteRole_WhenHasLinkedUser_ShouldReturnBadRequestException(){
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.countByRolesContaining(role)).thenReturn(1L);

        BadRequestException exception = assertThrows(BadRequestException.class,()->
                roleService.deleteRole(1L));

        //assert
        assertEquals("Role is currently in use and cannot be deleted.",exception.getMessage());
        verify(roleRepository,never()).delete(any(Role.class));
    }
}
