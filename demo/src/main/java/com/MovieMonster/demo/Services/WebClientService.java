package com.MovieMonster.demo.Services;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WebClientService {
    @Bean
    public WebClient apiClient() {
        return WebClient.create("https://api.themoviedb.org");
    }
}
