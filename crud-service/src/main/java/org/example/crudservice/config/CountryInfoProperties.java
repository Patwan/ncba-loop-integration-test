package org.example.crudservice.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "soap.client.country-info")
public class CountryInfoProperties {
    private String endpointUrl;
    private int connectTimeout = 5000;
    private int readTimeout = 10000;
}