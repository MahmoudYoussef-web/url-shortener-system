package com.mahmoudyoussef.url_shortener.controller;

import com.mahmoudyoussef.url_shortener.config.ClientIpResolver;
import com.mahmoudyoussef.url_shortener.dto.request.ShortenRequest;
import com.mahmoudyoussef.url_shortener.dto.response.ShortenResponse;
import com.mahmoudyoussef.url_shortener.service.RateLimiterService;
import com.mahmoudyoussef.url_shortener.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Shortener API", description = "Operations for URL shortening")
@Validated
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final RateLimiterService rateLimiterService;
    private final ClientIpResolver clientIpResolver;

    public UrlController(UrlService urlService,
                         RateLimiterService rateLimiterService,
                         ClientIpResolver clientIpResolver) {
        this.urlService = urlService;
        this.rateLimiterService = rateLimiterService;
        this.clientIpResolver = clientIpResolver;
    }

    @Operation(summary = "Shorten URL")
    @ApiResponse(responseCode = "201", description = "URL shortened successfully")
    @PostMapping
    public ResponseEntity<ShortenResponse> shortenUrl(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = clientIpResolver.resolve(httpRequest);
        rateLimiterService.checkRateLimit("rate:shorten:" + clientIp);

        log.info("POST /urls | ip={} | url={}", clientIp, request.getUrl());

        ShortenResponse response = urlService.shorten(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{code}/stats")
    public ResponseEntity<ShortenResponse> getStats(
            @PathVariable @NotBlank String code,
            HttpServletRequest httpRequest) {

        String clientIp = clientIpResolver.resolve(httpRequest);
        rateLimiterService.checkRateLimit("rate:stats:" + clientIp);

        log.info("GET /urls/{}/stats | ip={}", code, clientIp);

        ShortenResponse response = urlService.getStats(code);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Redirect to original URL")
    @ApiResponse(responseCode = "302", description = "Redirect successful")
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable @NotBlank String code,
            HttpServletRequest httpRequest) {

        String clientIp = clientIpResolver.resolve(httpRequest);
        rateLimiterService.checkRateLimit("rate:redirect:" + clientIp);

        log.info("GET /urls/{} | ip={}", code, clientIp);

        String originalUrl = urlService.resolve(code);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}