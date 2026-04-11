package com.mahmoudyoussef.url_shortener.service.impl;

import com.mahmoudyoussef.url_shortener.dto.request.ShortenRequest;
import com.mahmoudyoussef.url_shortener.dto.response.ShortenResponse;
import com.mahmoudyoussef.url_shortener.exception.DuplicateAliasException;
import com.mahmoudyoussef.url_shortener.exception.UrlNotFoundException;
import com.mahmoudyoussef.url_shortener.generator.Base62Generator;
import com.mahmoudyoussef.url_shortener.generator.RedisIdGenerator;
import com.mahmoudyoussef.url_shortener.entity.UrlMapping;
import com.mahmoudyoussef.url_shortener.repository.ClickTrackingRepository;
import com.mahmoudyoussef.url_shortener.repository.ShardedUrlRepository;
import com.mahmoudyoussef.url_shortener.service.CacheService;
import com.mahmoudyoussef.url_shortener.service.UrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final ShardedUrlRepository repository;
    private final CacheService cacheService;
    private final Base62Generator base62Generator;
    private final RedisIdGenerator redisIdGenerator;
    private final ClickTrackingRepository clickTrackingRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CACHE_PREFIX = "url:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {

        log.info("Shorten request | url={} | alias={}",
                request.getUrl(), request.getCustomAlias());

        String code;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {

            code = request.getCustomAlias();

            if (repository.existsByShortCode(code)) {
                throw new DuplicateAliasException("Alias already exists: " + code);
            }

        } else {
            code = generateUniqueCodeWithRetry();
        }

        Duration ttl = request.getExpirationSeconds() != null
                ? Duration.ofSeconds(request.getExpirationSeconds())
                : DEFAULT_TTL;

        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC)
                .plusSeconds(ttl.getSeconds());

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(code);
        mapping.setLongUrl(request.getUrl());
        mapping.setExpiresAt(expiresAt);

        repository.save(mapping);

        cacheService.put(CACHE_PREFIX + code, request.getUrl(), ttl);

        return ShortenResponse.withClicks(baseUrl + code, code, 0L);
    }

    @Override
    public String resolve(String code) {

        log.info("Resolving code={}", code);

        String cacheKey = CACHE_PREFIX + code;

        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            clickTrackingRepository.increment(code);
            return cached;
        }


        String url = repository.findLongUrl(code);

        if (url == null) {
            throw new UrlNotFoundException("URL not found or expired: " + code);
        }

        cacheService.put(cacheKey, url, DEFAULT_TTL);

        clickTrackingRepository.increment(code);

        return url;
    }
    public ShortenResponse getStats(String code) {

        log.info("Fetching stats for code={}", code);

        String url = repository.findLongUrl(code);

        if (url == null) {
            throw new UrlNotFoundException("URL not found or expired: " + code);
        }

        long clicks = clickTrackingRepository.getClicks(code);

        return ShortenResponse.withClicks(baseUrl + code, code, clicks);
    }

    private String generateUniqueCodeWithRetry() {

        int attempts = 0;

        while (attempts < 5) {
            try {
                long id = redisIdGenerator.getNextId();
                String code = base62Generator.encode(id);

                if (!repository.existsByShortCode(code)) {
                    return code;
                }

            } catch (Exception e) {
                log.warn("Error generating code, retrying...", e);
            }

            attempts++;
        }

        throw new IllegalStateException("Failed to generate unique code after retries");
    }
}