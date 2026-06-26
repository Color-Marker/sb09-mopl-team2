package com.sb09.sb09moplteam2.content.batch.Tmdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tmdb.api")
public record TmdbProperties(
    String key,
    String baseUrl
) {}
