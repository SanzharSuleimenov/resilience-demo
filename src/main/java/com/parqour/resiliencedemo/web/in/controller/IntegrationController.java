package com.parqour.resiliencedemo.web.in.controller;

import com.parqour.resiliencedemo.domain.Todo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequestMapping("/api/it")
@RequiredArgsConstructor
public class IntegrationController {

  private final RestClient wiremockRestClient;

  @GetMapping
  public Todo[] get() {
    log.info("Controller method called");
    return wiremockRestClient
        .get()
        .uri("/todos")
        .retrieve()
        .body(Todo[].class);
  }
}
