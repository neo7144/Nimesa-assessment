package com.example.AWS.configurtion.aws.cred.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AWSCredentialConfiguration {
    @Value("${aws.access.key.id}")
    private String accessId;

    @Value("${aws.secret.access.key}")
    private String accessKey;
}
