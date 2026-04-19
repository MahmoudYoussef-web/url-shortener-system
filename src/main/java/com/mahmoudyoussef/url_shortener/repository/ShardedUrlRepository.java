package com.mahmoudyoussef.url_shortener.repository;

import com.mahmoudyoussef.url_shortener.entity.UrlMapping;
import com.mahmoudyoussef.url_shortener.service.ShardRouter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
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
            DataSource ds = shardRouter.getDataSource(id);
            log.info("Initializing JdbcTemplate for shard {}", id);
            return new JdbcTemplate(ds);
        });
    }

    @PostConstruct
    public void init() {
        for (int shardId : shardRouter.getAllShardIds()) {
            jdbcTemplateMap.computeIfAbsent(shardId, id -> {
                DataSource ds = shardRouter.getDataSource(id);
                return new JdbcTemplate(ds);
            });
        }
    }

    public void save(UrlMapping mapping) {
        JdbcTemplate jdbc = getJdbcTemplate(mapping.getShortCode());

        jdbc.update(
                "INSERT INTO url_mapping (short_code, long_url, created_at, expires_at) VALUES (?, ?, ?, ?)",
                mapping.getShortCode(),
                mapping.getLongUrl(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt()
        );
    }

    public String findLongUrl(String shortCode) {
        JdbcTemplate jdbc = getJdbcTemplate(shortCode);

        try {
            return jdbc.queryForObject(
                    "SELECT long_url FROM url_mapping WHERE short_code = ? AND (expires_at IS NULL OR expires_at > NOW())",
                    String.class,
                    shortCode
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UrlMapping findEntityByShortCode(String shortCode) {
        JdbcTemplate jdbc = getJdbcTemplate(shortCode);

        try {
            return jdbc.queryForObject(
                    "SELECT short_code, long_url, created_at, expires_at FROM url_mapping " +
                            "WHERE short_code = ? AND (expires_at IS NULL OR expires_at > NOW())",
                    (rs, rowNum) -> {
                        UrlMapping m = new UrlMapping();
                        m.setShortCode(rs.getString("short_code"));
                        m.setLongUrl(rs.getString("long_url"));
                        m.setExpiresAt(rs.getObject("expires_at", LocalDateTime.class));
                        return m;
                    },
                    shortCode
            );
        } catch (EmptyResultDataAccessException e) {
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

    public int deleteExpired() {
        int total = 0;

        String sql = "DELETE FROM url_mapping WHERE expires_at IS NOT NULL AND expires_at <= NOW()";

        for (int shardId : jdbcTemplateMap.keySet()) {
            total += jdbcTemplateMap.get(shardId).update(sql);
        }

        return total;
    }
}