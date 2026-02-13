//package com.tech.aidocqna.security;
//
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//
//@Service
//public class RateLimitService {
//
//    private final StringRedisTemplate redisTemplate;
//
//    public RateLimitService(StringRedisTemplate redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    public boolean isAllowed(String key, long maxRequests, Duration window) {
//        Long current = redisTemplate.opsForValue().increment(key);
//        if (current == null) {
//            return false;
//        }
//        if (current == 1L) {
//            redisTemplate.expire(key, window);
//        }
//        return current <= maxRequests;
//    }
//}
