package com.mahmoudyoussef.url_shortener.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ShortenRequest {

    @NotBlank(message = "URL must not be blank")
    @Pattern(
            regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            message = "Invalid URL format"
    )
    private String url;

    @Min(value = 60, message = "Expiration must be at least 60 seconds")
    private Long expirationSeconds;

    @Pattern(
            regexp = "^[a-zA-Z0-9_-]{3,20}$",
            message = "Custom alias must be 3-20 characters and contain only letters, numbers, _ or -"
    )
    private String customAlias;

    public String getUrl() {
        return url;
    }

    public Long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setExpirationSeconds(Long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }
}