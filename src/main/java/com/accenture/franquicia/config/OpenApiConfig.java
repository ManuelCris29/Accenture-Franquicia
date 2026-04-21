package com.accenture.franquicia.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Franquicia API")
                        .version("1.0.0")
                        .description("API reactiva para gestión de franquicias, sucursales y productos")
                        .contact(new Contact()
                                .name("Accenture")
                                .email("mcristobaldev@gmail.com")))
                .tags(List.of(
                        new Tag().name("Franquicias").description("Gestión de franquicias"),
                        new Tag().name("Sucursales").description("Gestión de sucursales"),
                        new Tag().name("Productos").description("Gestión de productos")
                ));
    }
}
