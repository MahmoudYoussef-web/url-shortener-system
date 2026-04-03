package com.mahmoudyoussef.url_shortener.controller;

import com.mahmoudyoussef.url_shortener.dto.request.ShortenRequest;
import com.mahmoudyoussef.url_shortener.dto.response.ShortenResponse;
import com.mahmoudyoussef.url_shortener.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Shortener API", description = "Operations for URL shortening")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(summary = "Shorten URL")
    @ApiResponse(responseCode = "201", description = "URL shortened successfully")
    @PostMapping
    public ResponseEntity<ShortenResponse> shortenUrl(
            @Valid @RequestBody ShortenRequest request) {

        ShortenResponse response = urlService.shorten(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Redirect to original URL")
    @ApiResponse(responseCode = "302", description = "Redirect successful")
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {

        String originalUrl = urlService.resolve(code);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    @GetMapping("/debug/{code}")
    public String debug(@PathVariable String code) {
        return urlService.resolve(code);
    }
}