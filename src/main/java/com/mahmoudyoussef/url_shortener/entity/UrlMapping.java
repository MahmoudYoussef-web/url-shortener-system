package com.mahmoudyoussef.url_shortener.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_mapping", indexes = {
        @Index(name = "idx_shortcode", columnList = "short_code", unique = true)
})
@Getter
@Setter
public class UrlMapping {

    @Id
    @Column(name = "short_code")
    private String shortCode;

    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}