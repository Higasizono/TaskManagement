package com.taskmanagement.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * `app.cors.allowed-origins` が {@link CorsProperties} に正しくバインドされることを検証する。
 *
 * <p>CorsConfig はコンストラクタで CorsProperties を受け取るため、バインドに失敗すると 起動時にコンテキストのロードが失敗する。DBを起動せずに確認できるよう
 * ApplicationContextRunner を使う。
 */
class CorsConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(CorsConfig.class);

    @Test
    void bindsSingleAllowedOrigin() {
        contextRunner
                .withPropertyValues("app.cors.allowed-origins=http://localhost:5173")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                                    .containsExactly("http://localhost:5173");
                        });
    }

    @Test
    void bindsCommaSeparatedAllowedOrigins() {
        contextRunner
                .withPropertyValues(
                        "app.cors.allowed-origins=http://localhost:5173,https://example.com")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                                    .containsExactly(
                                            "http://localhost:5173", "https://example.com");
                        });
    }
}
