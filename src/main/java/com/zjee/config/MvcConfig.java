package com.zjee.config;

import com.zjee.filter.IpCountFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableCaching
public class MvcConfig implements WebMvcConfigurer {
    /**
     * 注意：Filter属于Servlet层级，而interceptor属于SpringMVC层级，
     * 它归SpringMVC管理，而Filter归容器直接管理，因此两者配置方式不一致
     *
     * @param myFilter
     * @return
     */
    @Bean
    FilterRegistrationBean getFilterRegistrationBean(IpCountFilter myFilter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(myFilter);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setName("IpCountFilter");
        return filterRegistrationBean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

    }
}
