package ec2springboot.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.secretsmanager.secret-name}")
    private String secretName;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Autowired
    private Environment environment;

    @Bean
    @Profile({"local"})
    public S3Client localS3Client(
            @Value("${cloud.aws.credentials.secretKey}") String accessSecret,
            @Value("${cloud.aws.credentials.accessKey}") String accessKey) {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, accessSecret);
        builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        return builder.build();
    }

    @Bean
    @Profile({"aws_cloud"})
    public S3Client higherEnvS3Client() {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
        return builder.build();
    }

    @Bean
    @Primary
    @Profile("aws_cloud")
    public DataSource awsDataSource() {
        System.out.println("Creating AWS DataSource with Secrets Manager...");

        DatabaseCredentials credentials = getCredentialsFromSecretsManager();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool settings
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        config.setConnectionTestQuery("SELECT 1");

        System.out.println("DataSource created successfully with username: " + credentials.getUsername());
        return new HikariDataSource(config);
    }


    private DatabaseCredentials getCredentialsFromSecretsManager() {
        try {
            System.out.println("Fetching credentials from Secrets Manager...");
            System.out.println("Region: " + region);
            System.out.println("Secret: " + secretName);

            SecretsManagerClient client = SecretsManagerClient.builder()
                    .region(Region.of(region))
                    .build();

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode secretJson = mapper.readTree(response.secretString());

            DatabaseCredentials credentials = new DatabaseCredentials();
            credentials.setUsername(secretJson.get("username").asText());
            credentials.setPassword(secretJson.get("password").asText());

            System.out.println("Successfully retrieved credentials for user: " + credentials.getUsername());
            return credentials;

        } catch (Exception e) {
            System.err.println("Failed to retrieve credentials from Secrets Manager: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load database credentials", e);
        }
    }

    // Inner class for credentials
    public static class DatabaseCredentials {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}