package com.mahmoudyoussef.url_shortener.service;

public interface RateLimiterService {

    void checkRateLimit(String key);
}