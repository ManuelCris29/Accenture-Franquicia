package com.accenture.franquicia.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import java.net.URI;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.endpoint:}")
    private String awsEndpoint;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.secrets.name:/accenture/dev/db-credentials}")
    private String secretName;

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

    @Bean
    public DbCredentials dbCredentials(SecretsManagerClient secretsManagerClient) {
        try {
            log.info("Leyendo credenciales desde Secrets Manager: {}", secretName);

            String secretValue = secretsManagerClient
                .getSecretValue(
                    GetSecretValueRequest.builder()
                        .secretId(secretName)
                        .build()
                )
                .secretString();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(secretValue);

            String username = json.get("username").asText();
            String password = json.get("password").asText();
            String host = json.get("host").asText();
            String port = json.get("port").asText();
            String dbname = json.get("dbname").asText();

            log.info("Credenciales leídas para DB: {}", dbname);

            return new DbCredentials(username, password, host, port, dbname);

        } catch (Exception e) {
            log.error("Error leyendo secreto: {}", e.getMessage());
            throw new RuntimeException("No se pudo leer el secreto de AWS", e);
        }
    }

    public record DbCredentials(
        String username,
        String password,
        String host,
        String port,
        String dbname
    ) {}
}