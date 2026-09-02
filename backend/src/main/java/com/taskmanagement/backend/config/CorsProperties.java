package com.taskmanagement.backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** `app.cors.*` のバインド先。 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {}
