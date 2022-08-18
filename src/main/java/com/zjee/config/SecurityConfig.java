package com.zjee.config;

import com.alibaba.fastjson.JSON;
import com.zjee.constant.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //springBoot 5.0密码存储方式有变化，注意password方法改变了
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //手动配置数据库密码，这里直接使用明文好了
        auth.inMemoryAuthentication()
            .passwordEncoder(new BCryptPasswordEncoder())
            .withUser("zhongjie")
            .password(new BCryptPasswordEncoder().encode("zjee@web"))
            .roles(UserRole.ADMIN.name());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling()
            .authenticationEntryPoint(this::needLogin)
            .and().formLogin()
            .successHandler(this::onAuthSuccess)
            .failureHandler(this::onAuthFailure)
            .and()
            .authorizeRequests()
            .antMatchers("/api/sysInfo", "/api/visitLog/**").hasRole(UserRole.ADMIN.name())
            .antMatchers("/api/download/**").hasRole(UserRole.ADMIN.name())
            .antMatchers("/api/task/**").hasRole(UserRole.ADMIN.name())
            .anyRequest().permitAll();
        http.csrf().disable();
    }

    private void onAuthSuccess(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) throws IOException, ServletException {

        log.info("login success: {}", authentication);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("auth_status", 0);
        map.put("auth_data", authentication);
        response.getWriter().write(JSON.toJSONString(map));
    }

    private void onAuthFailure(HttpServletRequest request, HttpServletResponse response,
                              AuthenticationException e) throws IOException, ServletException {
        log.error("login failed: ", e);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("auth_status", 1);
        map.put("msg", e.getMessage());
        response.getWriter().write(JSON.toJSONString(map));
    }

    private void needLogin(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException e) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 302);
        map.put("msg", e.getMessage());
        response.getWriter().write(JSON.toJSONString(map));
    }
}
