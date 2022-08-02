package com.spankinfresh.blog.api;

import com.spankinfresh.blog.domain.BlogPost;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/articles")
public class BlogPostController {

    @PostMapping
    public ResponseEntity<BlogPost> createBlogEntry(@RequestBody BlogPost blogPost, UriComponentsBuilder uriComponentsBuilder) {
        UriComponents uriComponents = uriComponentsBuilder.path("/api/articles/{id}").buildAndExpand(blogPost.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", uriComponents.toUri().toString());
        return new ResponseEntity<>(blogPost, headers, HttpStatus.CREATED);
    }
}
