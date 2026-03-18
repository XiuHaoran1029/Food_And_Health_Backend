package org.example.food_a.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 或者允许所有 (开发环境方便，生产环境慎用)
        config.addAllowedOriginPattern("*");

        config.addAllowedMethod("*"); // 允许所有请求方法 (GET, POST, PUT, DELETE, OPTIONS等)
        config.addAllowedHeader("*"); // 允许所有请求头
        config.setAllowCredentials(true); // 如果涉及 Cookie，必须设置为 true

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}