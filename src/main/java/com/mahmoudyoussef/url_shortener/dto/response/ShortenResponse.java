package com.mahmoudyoussef.url_shortener.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenResponse {

    private final String shortUrl;
    private final String code;
    private final Long clickCount;

    public ShortenResponse(String shortUrl, String code, Long clickCount) {
        this.shortUrl = shortUrl;
        this.code = code;
        this.clickCount = clickCount;
    }

    public static ShortenResponse of(String shortUrl, String code) {
        return new ShortenResponse(shortUrl, code, 0L);
    }

    public static ShortenResponse withClicks(String shortUrl, String code, Long clickCount) {
        return new ShortenResponse(shortUrl, code, clickCount);
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getCode() {
        return code;
    }

    public Long getClickCount() {
        return clickCount;
    }
}