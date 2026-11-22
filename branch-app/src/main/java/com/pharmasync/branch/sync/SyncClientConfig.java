package com.pharmasync.branch.sync;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
public class SyncClientConfig {

    @Bean
    public RestClient branchRestClient(RestTemplateBuilder builder) {
        return RestClient.builder()
                .requestFactory(builder.build().getRequestFactory())
                .build();
    }
}
