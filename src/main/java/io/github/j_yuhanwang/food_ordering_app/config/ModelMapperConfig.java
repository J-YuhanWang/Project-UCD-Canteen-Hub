package io.github.j_yuhanwang.food_ordering_app.config;/*
 * @author BlairWang
 * @Date 21/12/2025 9:32 pm
 * @Version 1.0
 */

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ModelMapper bean.
 * ModelMapper is used for converting between Entities and DTOs.
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Create and configure ModelMapper instance.
     * @return a configured ModelMapper bean for the Spring context.
     */
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        // Configuration settings:
        modelMapper.getConfiguration()
                // Enable access to private fields directly
                // 允许 ModelMapper 直接访问私有属性，不需要 Getter/Setter
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)

                // Enable field matching (e.g., matching 'userName' in Entity to 'userName' in DTO)
                // 开启字段匹配功能
                .setFieldMatchingEnabled(true)

                // Use Strict matching strategy (flexible mapping for similar field names)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;

    }
}
