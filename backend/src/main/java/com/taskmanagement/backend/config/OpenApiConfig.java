package com.taskmanagement.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI（/swagger-ui.html）に表示するAPIのメタデータ。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagementOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("タスク管理アプリ API")
                                .version("v1")
                                .description(
                                        "ボード・カラム・カードを管理するREST API。"
                                                + "カラムは「未着手」「進行中」「完了」の3種に固定され、ボード作成時に自動生成される。"));
    }
}
