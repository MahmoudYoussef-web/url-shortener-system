package com.mahmoudyoussef.url_shortener.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mahmoudyoussef.url_shortener.repository.ShardedUrlRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Profile("dev")
public class ShardTestController {

    private final ShardedUrlRepository repo;

    @GetMapping("/test/{code}")
    public String test(@PathVariable String code) {
        return repo.findLongUrl(code);
    }
}
