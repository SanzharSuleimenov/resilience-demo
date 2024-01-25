package com.parqour.resiliencedemo.web.in.controller;

import com.parqour.resiliencedemo.application.port.in.FetchPostsUseCase;
import com.parqour.resiliencedemo.domain.Post;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/json")
public class JsonPlaceholderController {

  private final FetchPostsUseCase fetchPostsUseCase;

  public JsonPlaceholderController(FetchPostsUseCase fetchPostsUseCase) {
    this.fetchPostsUseCase = fetchPostsUseCase;
  }

  @GetMapping
  public List<Post> getPosts() {
    return fetchPostsUseCase.fetchPosts();
  }
}
