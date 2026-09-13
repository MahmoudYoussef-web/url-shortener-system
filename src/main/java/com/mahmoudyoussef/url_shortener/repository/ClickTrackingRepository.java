package com.mahmoudyoussef.url_shortener.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ClickTrackingRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "click:";

    public void increment(String code) {
        try {
            redisTemplate.opsForValue().increment(PREFIX + code);
        } catch (Exception e) {
            log.error("Click increment failed (fail-open) for code={}", code, e);
        }
    }

    public long getClicks(String code) {
        try {
            String value = redisTemplate.opsForValue().get(PREFIX + code);
            return value == null ? 0 : Long.parseLong(value);
        } catch (Exception e) {
            log.error("Click read failed (fail-open) for code={}", code, e);
            return 0;
        }
    }
}