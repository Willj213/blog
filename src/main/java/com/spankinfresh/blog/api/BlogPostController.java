package com.spankinfresh.blog.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class BlogPostController {

    @PostMapping
    public ResponseEntity createBlogEntry() {
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
