package com.mahmoudyoussef.url_shortener.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.shards")
@Getter
@Setter
public class ShardDataSourceProperties {

    private Map<Integer, Shard> datasource;

    @Getter
    @Setter
    public static class Shard {
        private String url;
        private String username;
        private String password;
    }
}