package com.samuel.people_api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.nationalize")
public record NationalizeProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
