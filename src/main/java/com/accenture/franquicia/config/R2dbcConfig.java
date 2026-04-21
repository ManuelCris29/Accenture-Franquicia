package com.accenture.franquicia.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import io.r2dbc.spi.ConnectionFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class R2dbcConfig {

    private final AwsConfig.DbCredentials dbCredentials;

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        log.info("Configurando R2DBC con host: {}", dbCredentials.host());

        MySqlConnectionConfiguration config = MySqlConnectionConfiguration.builder()
            .host(dbCredentials.host())
            .port(Integer.parseInt(dbCredentials.port()))
            .database(dbCredentials.dbname())
            .username(dbCredentials.username())
            .password(dbCredentials.password())
            .build();

        return MySqlConnectionFactory.from(config);
    }
}