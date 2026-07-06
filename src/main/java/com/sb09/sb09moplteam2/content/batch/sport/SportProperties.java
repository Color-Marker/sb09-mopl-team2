package com.sb09.sb09moplteam2.content.batch.sport;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sports.api")
public record SportProperties(String key, String baseUrl) {}