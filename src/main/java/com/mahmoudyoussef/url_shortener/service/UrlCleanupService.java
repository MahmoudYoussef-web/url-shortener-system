package com.mahmoudyoussef.url_shortener.service;

import com.mahmoudyoussef.url_shortener.repository.ShardedUrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlCleanupService {

    private final ShardedUrlRepository repository;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredUrls() {

        try {
            int deleted = repository.deleteExpired();

            log.info("Cleanup job executed, deleted {} expired URLs", deleted);

        } catch (Exception e) {
            log.error("Cleanup job failed", e);
        }
    }
}