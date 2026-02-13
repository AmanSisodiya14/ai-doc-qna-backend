//package com.tech.aidocqna.security;
//
//import com.tech.aidocqna.config.AppProperties;
//import com.tech.aidocqna.dto.ApiResponse;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
//
//@Component
//public class RateLimitFilter extends OncePerRequestFilter {
//
//    private static final DateTimeFormatter MINUTE_FORMATTER =
//        DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC);
//
//    private final RateLimitService rateLimitService;
//    private final AppProperties appProperties;
//    private final ObjectMapper objectMapper;
//
//    public RateLimitFilter(RateLimitService rateLimitService, AppProperties appProperties, ObjectMapper objectMapper) {
//        this.rateLimitService = rateLimitService;
//        this.appProperties = appProperties;
//        this.objectMapper = objectMapper;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        return path.startsWith("/actuator");
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//        throws ServletException, IOException {
//        String minuteBucket = MINUTE_FORMATTER.format(Instant.now());
//        String identity = resolveIdentity(request);
//        String key = "rate-limit:" + identity + ":" + minuteBucket;
//
//        boolean allowed = rateLimitService.isAllowed(
//            key,
//            appProperties.getRateLimit().getRequestsPerMinute(),
//            Duration.ofMinutes(2)
//        );
//        if (!allowed) {
//            response.setStatus(429);
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error("Rate limit exceeded")));
//            return;
//        }
//        filterChain.doFilter(request, response);
//    }
//
//    private String resolveIdentity(HttpServletRequest request) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()
//            && !"anonymousUser".equals(authentication.getName())) {
//            return "user:" + authentication.getName();
//        }
//        String ip = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
//        return "ip:" + ip;
//    }
//}
