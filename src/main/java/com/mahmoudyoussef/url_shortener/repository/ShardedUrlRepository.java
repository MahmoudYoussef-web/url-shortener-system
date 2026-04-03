package com.mahmoudyoussef.url_shortener.repository;

import com.mahmoudyoussef.url_shortener.entity.UrlMapping;
import com.mahmoudyoussef.url_shortener.service.ShardRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ShardedUrlRepository {

    private final ShardRouter shardRouter;


    private final Map<Integer, JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<>();

    private JdbcTemplate getJdbcTemplate(String code) {
        int shardId = shardRouter.getShardId(code);

        return jdbcTemplateMap.computeIfAbsent(shardId, id -> {
            DataSource ds = shardRouter.route(code);
            log.info("Initializing JdbcTemplate for shard {}", id);
            return new JdbcTemplate(ds);
        });
    }


    public void save(UrlMapping mapping) {
        JdbcTemplate jdbc = getJdbcTemplate(mapping.getShortCode());

        jdbc.update(
                "INSERT INTO url_mapping (short_code, long_url, created_at) VALUES (?, ?, ?)",
                mapping.getShortCode(),
                mapping.getLongUrl(),
                mapping.getCreatedAt()
        );

        log.debug("Saved URL mapping | code={}", mapping.getShortCode());
    }


    public String findLongUrl(String shortCode) {
        JdbcTemplate jdbc = getJdbcTemplate(shortCode);

        try {
            return jdbc.queryForObject(
                    "SELECT long_url FROM url_mapping WHERE short_code = ?",
                    String.class,
                    shortCode
            );
        } catch (EmptyResultDataAccessException e) {
            log.warn("URL not found in DB | code={}", shortCode);
            return null;
        }
    }


    public boolean existsByShortCode(String shortCode) {
        JdbcTemplate jdbc = getJdbcTemplate(shortCode);

        Integer result = jdbc.queryForObject(
                "SELECT COUNT(1) FROM url_mapping WHERE short_code = ?",
                Integer.class,
                shortCode
        );

        return result != null && result > 0;
    }
    }
