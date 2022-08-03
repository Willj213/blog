package com.spankinfresh.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spankinfresh.blog.data.BlogPostRepository;
import com.spankinfresh.blog.domain.BlogPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BlogPostControllerTests {

    @MockBean
    private BlogPostRepository mockRepository;
    private static final String RESOURCE_URI = "/api/articles";
    private final ObjectMapper mapper = new ObjectMapper();
    private static final BlogPost testPosting = new BlogPost(0L, "category", null, "title", "content");
    private static final BlogPost savedPosting = new BlogPost(1l, "category", LocalDateTime.now(), "title", "content");

    @Test
    @DisplayName("T01 - POST accepts and returns blog post representation")
    public void postCreatesNewBlogEntry_Test(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.save(refEq(testPosting, "datePosted"))).thenReturn(savedPosting);
        MvcResult result = mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testPosting)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedPosting.getId()))
                .andExpect(jsonPath("$.title").value(savedPosting.getTitle()))
                .andExpect(jsonPath("$.datePosted").value(savedPosting.getDatePosted().toString().substring(0, savedPosting.getDatePosted().toString().length() - 2)))
                .andExpect(jsonPath("$.category").value(savedPosting.getCategory()))
                .andExpect(jsonPath("$.content").value(savedPosting.getContent()))
                .andReturn();
        MockHttpServletResponse mockResponse = result.getResponse();
        assertEquals(String.format("http://localhost/api/articles/%d", savedPosting.getId()), mockResponse.getHeader("Location"));
        verify(mockRepository, times(1)).save(refEq(testPosting, "datePosted"));
        verifyNoMoreInteractions(mockRepository);
    }

}
