package com.mahmoudyoussef.url_shortener.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ShardDataSourceConfig {

    private final com.mahmoudyoussef.url_shortener.config.ShardDataSourceProperties properties;

    @Bean
    public Map<Integer, DataSource> shardDataSources() {

        Map<Integer, DataSource> shards = new HashMap<>();

        properties.getDatasource().forEach((shardId, shardProps) -> {

            DataSource dataSource = DataSourceBuilder.create()
                    .url(shardProps.getUrl())
                    .username(shardProps.getUsername())
                    .password(shardProps.getPassword())
                    .build();

            shards.put(shardId, dataSource);
        });

        return shards;
    }
}