package com.cvns.config;import org.springframework.context.annotation.*;import org.springframework.web.client.RestClient;
@Configuration public class AppConfig{@Bean RestClient.Builder restClientBuilder(){return RestClient.builder();}}
