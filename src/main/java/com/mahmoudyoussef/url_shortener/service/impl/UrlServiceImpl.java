package com.mahmoudyoussef.url_shortener.service.impl;

import com.mahmoudyoussef.url_shortener.dto.request.ShortenRequest;
import com.mahmoudyoussef.url_shortener.dto.response.ShortenResponse;
import com.mahmoudyoussef.url_shortener.exception.DuplicateAliasException;
import com.mahmoudyoussef.url_shortener.exception.UrlNotFoundException;
import com.mahmoudyoussef.url_shortener.generator.Base62Generator;
import com.mahmoudyoussef.url_shortener.entity.UrlMapping;
import com.mahmoudyoussef.url_shortener.repository.ShardedUrlRepository;
import com.mahmoudyoussef.url_shortener.service.CacheService;
import com.mahmoudyoussef.url_shortener.service.UrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final ShardedUrlRepository repository;
    private final CacheService cacheService;
    private final Base62Generator base62Generator;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CACHE_PREFIX = "url:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    @Override
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
            code = generateUniqueCode();
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(code);
        mapping.setLongUrl(request.getUrl());

        repository.save(mapping);

        Duration ttl = request.getExpirationSeconds() != null
                ? Duration.ofSeconds(request.getExpirationSeconds())
                : DEFAULT_TTL;

        cacheService.put(CACHE_PREFIX + code, request.getUrl(), ttl);

        return ShortenResponse.of(baseUrl + code, code);
    }

    @Override
    public String resolve(String code) {

        log.info("Resolving code={}", code);

        String cacheKey = CACHE_PREFIX + code;

        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String url = repository.findLongUrl(code);

        if (url == null) {
            throw new UrlNotFoundException("URL not found for code: " + code);
        }

        cacheService.put(cacheKey, url, DEFAULT_TTL);

        return url;
    }

    private String generateUniqueCode() {

        String code;
        int attempts = 0;

        do {
            code = base62Generator.encode(System.currentTimeMillis())
                    + UUID.randomUUID().toString().substring(0, 4);

            attempts++;

            if (attempts > 5) {
                throw new IllegalStateException("Failed to generate unique code");
            }

        } while (repository.existsByShortCode(code));

        return code;
    }
}