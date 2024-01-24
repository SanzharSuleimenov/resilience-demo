package com.parqour.resiliencedemo.web.in.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/it")
@RequiredArgsConstructor
public class IntegrationController {

  private final RestClient wiremockRestClient;

  @GetMapping
  public ArrayNode get() {
    return wiremockRestClient
        .get()
        .uri("/todos")
        .retrieve()
        .body(ArrayNode.class);
  }
}
