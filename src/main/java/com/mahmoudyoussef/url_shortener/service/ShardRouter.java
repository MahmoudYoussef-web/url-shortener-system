package com.mahmoudyoussef.url_shortener.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShardRouter {

    private final Map<Integer, DataSource> shardDataSources;
    private static final int SHARD_COUNT = 1;

    public DataSource route(String shortCode) {
        return shardDataSources.get(getShardId(shortCode));
    }

    public int getShardId(String shortCode) {
        return Math.abs(shortCode.hashCode()) % SHARD_COUNT;
    }
}