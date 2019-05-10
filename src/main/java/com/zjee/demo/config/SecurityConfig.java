package com.zjee.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${web.security.username}")
    String userName;
    @Value("${web.security.password}")
    String password;

    //springBoot 5.0密码存储方式有变化，注意password方法改变了
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .passwordEncoder(new BCryptPasswordEncoder())
            .withUser(userName)
            .password(new BCryptPasswordEncoder().encode(password))
            .roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .formLogin().and()
            .authorizeRequests()
            .antMatchers("/server","/jvm","/download/**","/startDownload","/visitor/**")
            .authenticated()
            .anyRequest().permitAll();
        http.csrf().disable();
    }
}
