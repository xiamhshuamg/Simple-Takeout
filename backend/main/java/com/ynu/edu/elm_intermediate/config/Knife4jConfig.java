package com.ynu.edu.elm_intermediate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j + springdoc 配置
 * 引入依赖后，启动项目访问：
 *   http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI elmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("外卖平台接口文档")
                        .version("1.0")
                        .description("Elm Intermediate 项目接口文档"));
    }

    @Bean
    public GroupedOpenApi elmApi() {
        return GroupedOpenApi.builder()
                .group("elm-api")
                // 把你现在的三个模块都加进文档里
                .pathsToMatch("/business/**", "/order/**", "/user/**")
                .build();
    }
}
