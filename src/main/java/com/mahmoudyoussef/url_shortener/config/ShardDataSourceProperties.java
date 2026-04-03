package com.mahmoudyoussef.url_shortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.shards")
public class ShardDataSourceProperties {

    private Map<Integer, Shard> datasource;

    public Map<Integer, Shard> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<Integer, Shard> datasource) {
        this.datasource = datasource;
    }

    public static class Shard {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}