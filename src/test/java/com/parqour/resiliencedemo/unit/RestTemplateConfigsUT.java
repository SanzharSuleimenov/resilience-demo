package com.parqour.resiliencedemo.unit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.domain.Post;
import com.parqour.resiliencedemo.web.in.controller.JsonPlaceholderController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest(classes = ResilienceDemoApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class RestTemplateConfigsUT {

  @Autowired
  private JsonPlaceholderController controller;

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("json-placeholder.base-url", wireMock::baseUrl);
    dynamicPropertyRegistry.add("rest-client.base-url", wireMock::baseUrl);
  }

  @BeforeEach
  void setUp() {
    wireMock.resetAll();
  }

  @Test
  void restTemplateRequest200() {
    wireMock.stubFor(
        WireMock.get("/posts")
            .willReturn(okJson("""
                [
                  {
                    "userId": 1,
                    "id": 1,
                    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                    "body": "quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto"
                  },
                  {
                    "userId": 1,
                    "id": 2,
                    "title": "qui est esse",
                    "body": "est rerum tempore vitae\\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\\nqui aperiam non debitis possimus qui neque nisi nulla"
                  },
                  {
                    "userId": 1,
                    "id": 3,
                    "title": "ea molestias quasi exercitationem repellat qui ipsa sit aut",
                    "body": "et iusto sed quo iure\\nvoluptatem occaecati omnis eligendi aut ad\\nvoluptatem doloribus vel accusantium quis pariatur\\nmolestiae porro eius odio et labore et velit aut"
                  }
                ]
                """))
    );

    List<Post> posts = controller.getPosts();
    assertNotNull(posts);
    assertEquals(3, posts.size());
    assertEquals(1, posts.get(0).userId());
    assertEquals(1, posts.get(1).userId());
    assertEquals(1, posts.get(2).userId());
    assertEquals(1, posts.get(0).id());
    assertEquals(2, posts.get(1).id());
    assertEquals(3, posts.get(2).id());
  }

  @Test
  void requestReadTimeout() {
    wireMock.stubFor(WireMock.get("/posts")
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(3500))
    );
    try {
      controller.getPosts();
      fail("Rest Template should throw exception, and this line should be unreachable");
    } catch (Exception e) {
      assertEquals(ResourceAccessException.class, e.getClass());
    }
  }

  @Test
  void upstreamServiceInternalServerError() {
    wireMock.stubFor(WireMock.get("/posts")
        .willReturn(WireMock.status(500))
    );
    try {
      controller.getPosts();
      fail("Rest Template should throw exception, and this line should be unreachable");
    } catch (Exception e) {
      assertEquals(HttpServerErrorException.InternalServerError.class, e.getClass());
    }
  }

  @Test
  void upstreamServiceNotImplemented() {
    wireMock.stubFor(WireMock.get("/posts")
        .willReturn(WireMock.status(501)));
    try {
      controller.getPosts();
      fail("Rest Template should throw exception, and this line should be unreachable");
    } catch (Exception e) {
      assertEquals(HttpServerErrorException.NotImplemented.class, e.getClass());
    }
  }

  @Test
  void upstreamServiceBadGateway() {
    wireMock.stubFor(WireMock.get("/posts")
        .willReturn(WireMock.status(502)));
    try {
      controller.getPosts();
      fail("Rest Template should throw exception, and this line should be unreachable");
    } catch (Exception e) {
      assertEquals(HttpServerErrorException.BadGateway.class, e.getClass());
    }
  }

  @Test
  void upstreamServiceForbidden() {
    wireMock.stubFor(WireMock.get("/posts")
        .willReturn(WireMock.status(403)));
    try {
      controller.getPosts();
      fail("Rest Template should throw exception, and this line should be unreachable");
    } catch (Exception e) {
      assertEquals(HttpClientErrorException.Forbidden.class, e.getClass());
    }
  }
}
