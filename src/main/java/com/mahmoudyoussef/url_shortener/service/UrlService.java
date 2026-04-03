package com.mahmoudyoussef.url_shortener.service;

import com.mahmoudyoussef.url_shortener.dto.request.ShortenRequest;
import com.mahmoudyoussef.url_shortener.dto.response.ShortenResponse;

public interface UrlService {

    ShortenResponse shorten(ShortenRequest request);

    String resolve(String code);
}