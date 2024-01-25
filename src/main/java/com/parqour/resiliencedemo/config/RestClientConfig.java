package com.parqour.resiliencedemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RestClientConfig {

  @Bean
  public RestClient wiremockRestClient(
      @Value("${rest-client.base-url}") String baseUrl,
      RestClient.Builder builder) {
    log.info("Initializing RestClient {}", baseUrl);
    return builder
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
