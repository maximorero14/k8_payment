package com.maximorero14.payment.rest_client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configure timeouts
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        restTemplate.setRequestFactory(factory);

        // Add error handler that doesn't throw exceptions for 4xx/5xx
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // Never consider responses as errors - let our client handle them
            }

        });

        return restTemplate;
    }
}