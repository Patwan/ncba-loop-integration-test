package org.example.crudservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "soap.client.country-info")
public class CountryInfoProperties {
    private String endpointUrl;
    private int connectTimeout = 5000;
    private int readTimeout = 10000;
}