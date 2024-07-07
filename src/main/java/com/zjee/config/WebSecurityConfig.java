package com.zjee.config;

import com.zjee.common.util.JsonUtil;
import com.zjee.constant.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig {

    @Autowired
    DataSource dataSource;

    //springBoot 5.0密码存储方式有变化，注意password方法改变了
    @Bean
    public UserDetailsManager userDetailsManager() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager();
        manager.setDataSource(dataSource);
        return manager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(req ->
                req.requestMatchers("/api/sysInfo", "/api/visitLog/**","/api/sysInfo",
                                "/api/visitLog/**","/api/download/**","/api/task/**")
                .hasAnyRole(UserRole.ADMIN.name()).anyRequest().permitAll())
                .exceptionHandling(exp -> exp.authenticationEntryPoint(this::needLogin))
                .formLogin(formLogin -> formLogin.successHandler(this::onAuthSuccess).failureHandler(this::onAuthFailure))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    private void onAuthSuccess(HttpServletRequest request, HttpServletResponse response,
                               Authentication authentication) throws IOException, ServletException {
        log.info("login success: {}", authentication);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("auth_status", 0);
        map.put("auth_data", authentication);
        response.getWriter().write(JsonUtil.toJson(map));
    }

    private void onAuthFailure(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException e) throws IOException, ServletException{
        log.error("login failed: {}", e.getMessage());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("auth_status", 1);
        map.put("msg", e.getMessage());
        response.getWriter().write(JsonUtil.toJson(map));
    }

    private void needLogin(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException e) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 302);
        map.put("msg", e.getMessage());
        response.getWriter().write(JsonUtil.toJson(map));
    }
}
