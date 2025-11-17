package com.example.week7.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsS3Config {

    private String accessKey;

    private String secretKey;

    private String region;

}
