package com.mahmoudyoussef.url_shortener.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClickTrackingRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "click:";

    public void increment(String code) {
        redisTemplate.opsForValue().increment(PREFIX + code);
    }

    public long getClicks(String code) {
        String value = redisTemplate.opsForValue().get(PREFIX + code);
        return value == null ? 0 : Long.parseLong(value);
    }
}