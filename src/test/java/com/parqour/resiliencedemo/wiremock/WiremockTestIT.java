package com.parqour.resiliencedemo.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WiremockTestIT {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("rest-client.base-url", wireMockServer::baseUrl);
  }

  @Autowired
  private RestClient restClient;

  @Test
  void testGetAllTodosShouldReturnDataFromServer() {
    wireMockServer.stubFor(
        WireMock.get("/api/it")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    [
                      {"userId": 1,"id": 1,"title": "Learn Spring Boot 3.0", "completed": false},
                      {"userId": 1,"id": 2,"title": "Learn WireMock", "completed": true}
                    ]
                    """))
    );

    var response = restClient.get()
        .uri("/api/it")
        .retrieve()
        .body(ArrayNode.class);

    assertNotNull(response);
    assertEquals(2, response.size());
  }

  @AfterEach
  void afterEach() {
    wireMockServer.resetAll();
  }
}
