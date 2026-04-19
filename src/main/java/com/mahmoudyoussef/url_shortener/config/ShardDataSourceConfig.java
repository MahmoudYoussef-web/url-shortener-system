package com.mahmoudyoussef.url_shortener.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ShardDataSourceConfig {

    private final ShardDataSourceProperties properties;

    @Bean
    public Map<Integer, DataSource> shardDataSources() {

        Map<Integer, DataSource> shards = new HashMap<>();

        properties.getDatasource().forEach((shardId, shardProps) -> {

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(shardProps.getUrl());
            ds.setUsername(shardProps.getUsername());
            ds.setPassword(shardProps.getPassword());

            ds.setMaximumPoolSize(10);
            ds.setMinimumIdle(2);
            ds.setIdleTimeout(30000);
            ds.setMaxLifetime(1800000);
            ds.setConnectionTimeout(30000);

            ds.setPoolName("shard-" + shardId + "-pool");

            shards.put(shardId, ds);
        });

        return shards;
    }
}