package com.example.NextSteps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.model:llama3.2}")
    private String aiModel;

    @Value("${ai.ollama.timeout-seconds:120}")
    private int timeout;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
    }

    @Bean
    public String aiModel() {
        return aiModel;
    }

    @Bean
    public int timeOut() {
        return timeout;
    }
}

