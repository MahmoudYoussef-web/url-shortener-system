package com.mahmoudyoussef.url_shortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.mahmoudyoussef.url_shortener.exception.TooManyRequestsException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiterService implements RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final int LIMIT = 10;
    private static final long WINDOW_SECONDS = 60L;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(
                "local current = redis.call('INCR', KEYS[1]) " +
                        "if current == 1 then " +
                        "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                        "end " +
                        "return current"
        );
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    @Override
    public void checkRateLimit(String key) {

        try {
            Long count = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    List.of(key),
                    String.valueOf(WINDOW_SECONDS)
            );

            if (count != null && count > LIMIT) {
                log.warn("Rate limit exceeded for key={}", key);
                throw new TooManyRequestsException("Too many requests. Try again later.");
            }

        } catch (TooManyRequestsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Rate limiter failed (fail-open) for key={}", key, e);
        }
    }
}