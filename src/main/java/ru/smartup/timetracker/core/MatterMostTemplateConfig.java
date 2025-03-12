package ru.smartup.timetracker.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

@Configuration
public class MatterMostTemplateConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}

