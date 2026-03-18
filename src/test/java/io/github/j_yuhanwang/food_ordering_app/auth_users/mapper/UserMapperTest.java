package io.github.j_yuhanwang.food_ordering_app.auth_users.mapper;

import io.github.j_yuhanwang.food_ordering_app.auth_users.dtos.UserDTO;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuhanWang
 * @Date 18/03/2026 3:15 pm
 */

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {
    private UserMapper userMapper;

    @BeforeEach
    void setUp(){
        userMapper = new UserMapper();
    }

    @Test
    @DisplayName("Happy path - Convert User to UserDTO")
    void toDTO_ShouldMapAllFieldsCorrectly(){
        //arrange
        User user = User.builder()
                .id(100L)
                .name("Blair")
                .email("blair@example.com")
                .phoneNumber("0871234567")
                .address("UCD Campus")
                .profileUrl("https://s3.aws.com/my-avatar.jpg")
                .userStatus(UserStatus.ACTIVE)
                .roles(List.of(Role.builder().name("ROLE_STUDENT").build()))
                .build();
        //act
        UserDTO dto = userMapper.toDTO(user);

        //assert
        assertNotNull(dto);
        assertEquals("Blair",dto.getName());
        assertEquals("https://s3.aws.com/my-avatar.jpg", dto.getProfileUrl());
        //mapped from UserStatus.ACTIVE, Enum -> boolean
        assertTrue(dto.isActive());
        assertEquals(1,dto.getRoles().size());
        assertEquals("ROLE_STUDENT",dto.getRoles().getFirst().getName());
    }

    @Test
    @DisplayName("Should map isActive to false when UserStatus is BANNED")
    void toDTO_ShouldSetIsActiveToFalse_WhenStatusIsBanned() {
        // Arrange
        User bannedUser = User.builder()
                .userStatus(UserStatus.BANNED)
                .roles(List.of())
                .build();

        // Act
        UserDTO dto = userMapper.toDTO(bannedUser);

        // Assert
        assertFalse(dto.isActive(), "When UserStatus is BANNED, isActive should be false");
    }
}
