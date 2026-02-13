package com.tech.aidocqna.config;

import com.tech.aidocqna.security.AppUserDetailsService;
import com.tech.aidocqna.security.JwtAuthenticationFilter;
import com.tech.aidocqna.security.JwtService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FilterConfig {
    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    public FilterConfig(JwtService jwtService, AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> urlFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtService, userDetailsService));
        registrationBean.addUrlPatterns("/api/chat/*");
        registrationBean.addUrlPatterns("/api/files/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
