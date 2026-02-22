package io.github.j_yuhanwang.food_ordering_app.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * @author YuhanWang
 * @Date 21/02/2026 11:45 am
 */

@Configuration
public class AwsConfig {

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.accessKeyId}")
    private String awsAccessKey;

    @Value("${aws.secretKey}")
    private String awsSecretKey;

    //without this annotation will raise "No beans of 'S3Client' type found." error
    @Bean
    public StaticCredentialsProvider staticCredentialsProvider(){
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey,awsSecretKey));
    }

    @Bean
    public S3Client s3Client(StaticCredentialsProvider credentialsProvider){
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
