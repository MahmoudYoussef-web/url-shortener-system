package com.mahmoudyoussef.url_shortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final int LIMIT = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public void checkRateLimit(String key) {

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, WINDOW);
            }

            if (count != null && count > LIMIT) {
                log.warn("Rate limit exceeded for key={}", key);
                throw new com.mahmoudyoussef.url_shortener.exception.TooManyRequestsException(
                        "Too many requests. Try again later."
                );
            }

        } catch (Exception e) {
            log.error("Rate limiter failed, allowing request (fail-open)", e);
        }
    }
}