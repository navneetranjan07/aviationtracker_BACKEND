package com.aerotrack.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class AppConfig {

     // Instantiates the RestClient bean so API Client classes can inject it.
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}