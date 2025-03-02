package com.contentanalytics.content_analytic_system.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Content Analytics System API")
                        .description("API documentation for the content analytics system")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("My Name")
                                .email("example.my@email.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT") ))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.example.com").description("Production Server")));
    }

}
