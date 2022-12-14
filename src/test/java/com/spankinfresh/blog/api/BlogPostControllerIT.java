package com.spankinfresh.blog.api;

import com.spankinfresh.blog.domain.Author;
import com.spankinfresh.blog.domain.BlogPost;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlogPostControllerIT {

    @LocalServerPort
    private int localServerPort;
    @Autowired
    private TestRestTemplate restTemplate;
    private static final String RESOURCE_URI = "http://localhost:%d/api/articles";
    private static final BlogPost testPosting = new BlogPost(0L, "category", null, "title", "content", null);

    @BeforeAll
    public void createAuthor() {
        ResponseEntity<Author> responseEntity = this.restTemplate.postForEntity(
                String.format("http://localhost:%d/api/authors", localServerPort),
                new Author(0L, "Jane", "Doe", "jane@doe.com"), Author.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        testPosting.setAuthor(responseEntity.getBody());
    }

    @Test
    @DisplayName("T01 - POSTed location includes server port when created")
    public void test_01() {
        ResponseEntity<BlogPost> responseEntity = this.restTemplate.postForEntity(String.format(RESOURCE_URI, localServerPort), testPosting, BlogPost.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(localServerPort, responseEntity.getHeaders().getLocation().getPort());
    }

    @Test
    @DisplayName("T02 - POST generates nonzero ID")
    public void test_02() {
        ResponseEntity<BlogPost> responseEntity = this.restTemplate.postForEntity(String.format(RESOURCE_URI, localServerPort), testPosting, BlogPost.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        BlogPost blogPostReturned = responseEntity.getBody();
        assertNotEquals(testPosting.getId(), blogPostReturned.getId());
        assertEquals(String.format(RESOURCE_URI + "/%d",localServerPort, blogPostReturned.getId()),responseEntity.getHeaders().getLocation().toString());
    }

    @Test
    @DisplayName("T03 - POST automatically adds datePosted")
    public void test_03(){
        ResponseEntity<BlogPost> responseEntity = this.restTemplate.postForEntity(String.format(RESOURCE_URI, localServerPort), testPosting, BlogPost.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody().getDatePosted());
    }
}
