package com.parqour.resiliencedemo.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.parqour.resiliencedemo.domain.Todo;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class WiremockTestIT {

  @LocalServerPort
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("rest-client.base-url", wireMock::baseUrl);
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  void mockUpstreamServiceResponse200() {
    wireMock.stubFor(
        WireMock.get(WireMock.urlMatching("/todos"))
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            )
    );

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/api/it")
        .then()
        .statusCode(200);
  }

  @Test
  void mockUpstreamServiceResponse200WithBody() {
    wireMock.stubFor(
        WireMock.get(WireMock.urlMatching("/todos"))
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    [
                        {
                          "userId": 1,
                          "id": 1,
                          "title": "delectus aut autem",
                          "completed": false
                        },
                        {
                          "userId": 1,
                          "id": 2,
                          "title": "quis ut nam facilis et officia qui",
                          "completed": false
                        }
                    ]
                    """)
            )
    );

    Todo[] todos = restTemplate.getForEntity("http://localhost:" + port + "/api/it", Todo[].class)
        .getBody();

    assertNotNull(todos);
    assertEquals(2, todos.length);
  }
}
