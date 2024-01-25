package com.parqour.resiliencedemo.application.port.in;

import com.parqour.resiliencedemo.domain.Post;
import java.util.List;

public interface FetchPostsUseCase {

  List<Post> fetchPosts();
}
