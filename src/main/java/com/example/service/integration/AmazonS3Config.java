package com.example.service.integration;

import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@ConfigProperties(prefix = "app.s3")
@Data
public class AmazonS3Config {

    private String endpoint;
    private String port;
    private String accessKey;
    private String secretKey;
    private String bucketName;

}
