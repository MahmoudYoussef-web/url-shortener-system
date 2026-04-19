package com.mahmoudyoussef.url_shortener.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ShardRouter {

    private final Map<Integer, DataSource> shardDataSources;

    public int getShardCount() {
        return shardDataSources.size();
    }

    public DataSource route(String shortCode) {
        return shardDataSources.get(getShardId(shortCode));
    }

    public int getShardId(String shortCode) {
        int shardCount = getShardCount();
        if (shardCount == 0) {
            throw new IllegalStateException("No shard datasources configured");
        }
        return Math.abs(shortCode.hashCode()) % shardCount;
    }

    public DataSource getDataSource(int shardId) {
        DataSource ds = shardDataSources.get(shardId);
        if (ds == null) {
            throw new IllegalArgumentException("No datasource found for shardId=" + shardId);
        }
        return ds;
    }

    public Set<Integer> getAllShardIds() {
        return shardDataSources.keySet();
    }
}