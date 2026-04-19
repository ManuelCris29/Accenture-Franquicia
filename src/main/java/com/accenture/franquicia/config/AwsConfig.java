package com.accenture.franquicia.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import java.net.URI;

@Slf4j
@Configuration

public class AwsConfig {

    @Value("${aws.endpoint:}")
    private String awsEndpoint;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        SecretsManagerClientBuilder builder = SecretsManagerClient.builder()
            .region(Region.of(awsRegion));

        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            log.info("Conectando a LocalStack en: {}", awsEndpoint);
            builder
                .endpointOverride(URI.create(awsEndpoint))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                    )
                );
        } else {
            log.info("Conectando a AWS Secrets Manager real");
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
    
}
