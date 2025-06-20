package ec2springboot.test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

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
}