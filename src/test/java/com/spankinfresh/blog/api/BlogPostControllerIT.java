package com.spankinfresh.blog.api;

import com.spankinfresh.blog.domain.BlogPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlogPostControllerIT {

    @LocalServerPort
    private int localServerPort;
    @Autowired
    private TestRestTemplate restTemplate;
    private static final String RESOURCE_URI = "http://localhost:%d/api/articles";
    private static final BlogPost testPosting = new BlogPost(0L, "category", null, "title", "content");

    @Test
    @DisplayName("T01 - POSTed location includes server port when created")
    public void test_01() {
        ResponseEntity<BlogPost> responseEntity = this.restTemplate.postForEntity(String.format(RESOURCE_URI, localServerPort), testPosting, BlogPost.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(localServerPort, responseEntity.getHeaders().getLocation().getPort());
    }

}
