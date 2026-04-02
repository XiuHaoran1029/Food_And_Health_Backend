package org.example.food_a.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // 前后端分离必须关
                .authorizeHttpRequests(auth -> auth
                        // ✅ 放行 所 有 接 口（开发环境用）
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}