package com.mahmoudyoussef.url_shortener.integration;

import com.mahmoudyoussef.url_shortener.entity.UrlMapping;
import com.mahmoudyoussef.url_shortener.repository.ShardedUrlRepository;
import com.mahmoudyoussef.url_shortener.service.ShardRouter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShardingIntegrationTest {

    @Autowired
    private ShardedUrlRepository repository;

    @Autowired
    private ShardRouter shardRouter;

    private final Map<Integer, JdbcTemplate> jdbcMap = new HashMap<>();

    @BeforeEach
    void setup() {
        int shardCount = shardRouter.getShardCount();

        for (int i = 0; i < shardCount; i++) {
            DataSource ds = shardRouter.getDataSource(i);
            jdbcMap.put(i, new JdbcTemplate(ds));
        }
    }

    @Test
    void should_distribute_data_across_shards() {

        int shardCount = shardRouter.getShardCount();
        assertThat(shardCount).isGreaterThanOrEqualTo(2);

        Map<Integer, Integer> shardInsertCount = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            String code = "code_" + i;

            UrlMapping mapping = new UrlMapping();
            mapping.setShortCode(code);
            mapping.setLongUrl("https://example.com/" + i);
            mapping.setCreatedAt(LocalDateTime.now());
            mapping.setExpiresAt(LocalDateTime.now().plusHours(1));

            repository.save(mapping);

            int shardId = shardRouter.getShardId(code);
            shardInsertCount.put(shardId,
                    shardInsertCount.getOrDefault(shardId, 0) + 1);
        }

        long shardsUsed = shardInsertCount.values().stream()
                .filter(count -> count > 0)
                .count();

        assertThat(shardsUsed).isGreaterThan(1);
    }

    @Test
    void should_persist_data_in_correct_shard() {

        String code = "verify_shard";

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(code);
        mapping.setLongUrl("https://verify.com");
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setExpiresAt(LocalDateTime.now().plusHours(1));

        repository.save(mapping);

        int expectedShard = shardRouter.getShardId(code);

        JdbcTemplate jdbc = jdbcMap.get(expectedShard);

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM url_mapping WHERE short_code = ?",
                Integer.class,
                code
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void should_not_write_to_wrong_shard() {

        String code = "isolation_test";

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(code);
        mapping.setLongUrl("https://isolation.com");
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setExpiresAt(LocalDateTime.now().plusHours(1));

        repository.save(mapping);

        int correctShard = shardRouter.getShardId(code);

        for (Map.Entry<Integer, JdbcTemplate> entry : jdbcMap.entrySet()) {

            int shardId = entry.getKey();
            JdbcTemplate jdbc = entry.getValue();

            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM url_mapping WHERE short_code = ?",
                    Integer.class,
                    code
            );

            if (shardId == correctShard) {
                assertThat(count).isEqualTo(1);
            } else {
                assertThat(count).isEqualTo(0);
            }
        }
    }
}