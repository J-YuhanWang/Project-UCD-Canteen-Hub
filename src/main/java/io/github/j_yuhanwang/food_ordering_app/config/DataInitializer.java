package io.github.j_yuhanwang.food_ordering_app.config;

import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import io.github.j_yuhanwang.food_ordering_app.role.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 16/03/2026 4:54 pm
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            // 如果 role 表是空的，我们就自动塞入默认角色
            if (roleRepository.count() == 0) {
                Role studentRole = Role.builder().name("ROLE_STUDENT").build();
                Role managerRole = Role.builder().name("ROLE_MANAGER").build();
                Role adminRole = Role.builder().name("ROLE_ADMIN").build();
                Role deliveryRole = Role.builder().name("ROLE_DELIVERY").build();

                roleRepository.saveAll(List.of(studentRole, adminRole, managerRole, deliveryRole));
                System.out.println("Default roles inserted into database.");
            }
        };
    }
}
