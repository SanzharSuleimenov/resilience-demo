package com.parqour.resiliencedemo.application.port.service;

import com.parqour.resiliencedemo.application.port.in.FetchPostsUseCase;
import com.parqour.resiliencedemo.domain.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonPlaceholderService implements FetchPostsUseCase {

  private final RestTemplate restTemplate;

  @Value("${json-placeholder.base-url}")
  private String baseUrl;

  @Override
  public List<Post> fetchPosts() {
    Post[] posts = restTemplate.getForObject(baseUrl + "/posts", Post[].class);
    assert posts != null;

    return List.of(posts);
  }
}
