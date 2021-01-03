package com.zjee.config;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.core.AVOSCloud;
import com.alibaba.fastjson.JSON;
import com.zjee.constant.Constant;
import com.zjee.constant.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    static {
        AVOSCloud.initialize(Constant.LEAN_CLOUD_APP_ID, Constant.LEAN_CLOUD_APP_KEY);
    }

    //springBoot 5.0密码存储方式有变化，注意password方法改变了
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(this::getUserByName)
            .passwordEncoder(NoOpPasswordEncoder.getInstance());//手动配置数据库密码，这里直接使用明文好了
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
//            .antMatchers("/api/task/**").hasRole(UserRole.ADMIN.name())
            .anyRequest().permitAll();
        http.csrf().disable();
    }

    /**
     * 获取用户信息
     *
     * @param name
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails getUserByName(String name) throws UsernameNotFoundException {
        AVQuery<AVObject> query = new AVQuery<>(Constant.USER_INFO_CLASS);
        List<AVObject> userList = query.whereEqualTo("user_name", name).find();
        if (CollectionUtils.isEmpty(userList)) {
            log.error("user not find: {}", name);
            throw new UsernameNotFoundException("can not find user: " + name);
        }
        AVObject userInfo = userList.get(0);
        UserDetails userDetails = User.builder()
            .username(userInfo.getString("user_name"))
            .password(userInfo.getString("password"))
            .roles(userInfo.getString("role"))
            .build();
        log.info("user details: {}", userDetails);
        return userDetails;
    }

    public void onAuthSuccess(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) throws IOException, ServletException {

        log.info("login success: {}", authentication);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("auth_status", 0);
        map.put("auth_data", authentication);
        response.getWriter().write(JSON.toJSONString(map));
    }

    public void onAuthFailure(HttpServletRequest request, HttpServletResponse response,
                              AuthenticationException e) throws IOException, ServletException {
        log.error("login failed: ", e);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("auth_status", 1);
        map.put("msg", e.getMessage());
        response.getWriter().write(JSON.toJSONString(map));
    }

    public void needLogin(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException e) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 302);
        map.put("msg", e.getMessage());
        response.getWriter().write(JSON.toJSONString(map));
    }
}
