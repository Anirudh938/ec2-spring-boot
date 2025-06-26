package ec2springboot.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.Arrays;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.secretsmanager.secret-name}")
    private String secretName;

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

    @PostConstruct
    public void loadPassword() {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("aws_cloud")) {
            System.out.println("Not aws_cloud profile - skipping Secrets Manager");
            return;
        }

        try {
            SecretsManagerClient client = SecretsManagerClient.builder()
                    .region(Region.of(region))
                    .build();

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse response = client.getSecretValue(request);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode secretJson = mapper.readTree(response.secretString());
            String password = secretJson.get("password").asText();

            System.setProperty("spring.datasource.password", password);

            System.out.println("Password loaded from Secrets Manager using EC2 instance profile!");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load password from Secrets Manager", e);
        }
    }
}