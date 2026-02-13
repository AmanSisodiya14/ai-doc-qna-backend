//package com.tech.aidocqna.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final RateLimitFilter rateLimitFilter;
//    private final AppUserDetailsService appUserDetailsService;
//    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
//
//    public SecurityConfig(
//        JwtAuthenticationFilter jwtAuthenticationFilter,
//        RateLimitFilter rateLimitFilter,
//        AppUserDetailsService appUserDetailsService,
//        RestAuthenticationEntryPoint restAuthenticationEntryPoint
//    ) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.rateLimitFilter = rateLimitFilter;
//        this.appUserDetailsService = appUserDetailsService;
//        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authenticationProvider(authenticationProvider())
//            .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/api/auth/**", "/actuator/health", "/error").permitAll()
//                .anyRequest().authenticated()
//            )
//            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//            .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(appUserDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
//        return configuration.getAuthenticationManager();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
