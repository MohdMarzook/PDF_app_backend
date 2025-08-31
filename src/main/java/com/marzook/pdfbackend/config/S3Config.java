package com.marzook.pdfbackend.config;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    private final Environment env;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private Region region;

    public S3Config(Environment env) {
        this.env = env;
    }


    @Bean
    public S3Client pdfs3Client() {
        this.accessKey = env.getProperty("PS3_ACCESS_KEY");
        this.secretKey = env.getProperty("PS3_SECRET_KEY");
        this.endpoint = env.getProperty("S3_ENDPOINT");
        this.region = Region.AP_SOUTHEAST_1;

        return S3Client.builder()
                .region(region) // change to your AWS region
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    @Bean
    public S3Client htmls3Client() {
        this.accessKey = env.getProperty("HS3_ACCESS_KEY");
        this.secretKey = env.getProperty("HS3_SECRET_KEY");
        this.endpoint = env.getProperty("S3_ENDPOINT");
        this.region = Region.AP_SOUTHEAST_1;

        return S3Client.builder()
                .region(region) // change to your AWS region
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }


    @Bean
    public S3Presigner pdfs3Presigner() {
        this.accessKey = env.getProperty("PS3_ACCESS_KEY");
        this.secretKey = env.getProperty("PS3_SECRET_KEY");
        this.endpoint = env.getProperty("S3_ENDPOINT");
        this.region = Region.AP_SOUTHEAST_1;


        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(region)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

    }

    @Bean
    public S3Presigner htmls3Presigner() {
        this.accessKey = env.getProperty("HS3_ACCESS_KEY");
        this.secretKey = env.getProperty("HS3_SECRET_KEY");
        this.endpoint = env.getProperty("S3_ENDPOINT");
        this.region = Region.AP_SOUTHEAST_1;


        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(region)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

    }

}
