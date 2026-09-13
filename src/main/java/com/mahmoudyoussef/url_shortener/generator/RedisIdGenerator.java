package com.mahmoudyoussef.url_shortener.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RedisIdGenerator {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY = "url:id:counter";
    private static final int BATCH_SIZE = 100;

    private final AtomicLong current = new AtomicLong(0);
    private final AtomicLong max = new AtomicLong(0);

    public synchronized long getNextId() {
        if (current.get() >= max.get()) {
            allocateBatch();
        }
        return current.incrementAndGet();
    }

    private void allocateBatch() {
        try {
            Long newMax = redisTemplate.opsForValue().increment(KEY, BATCH_SIZE);

            if (newMax == null) {
                throw new IllegalStateException("Redis returned null for increment");
            }

            long start = newMax - BATCH_SIZE;

            current.set(start);
            max.set(newMax);

        } catch (Exception e) {
            // Degraded mode: random base in a high range keeps ids unique-ish
            // across instances (millis could collide) and far from Redis ids.
            long fallback = ThreadLocalRandom.current().nextLong(1_000_000_000L, 4_000_000_000L);
            current.set(fallback);
            max.set(fallback + BATCH_SIZE);
        }
    }
}