package com.sb09.sb09moplteam2.content.batch.Sports;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sports.api")
public record SportsProperties(String key, String baseUrl) {}