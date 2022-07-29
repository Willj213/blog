package com.spankinfresh.blog.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BlogPostControllerTests {

    private static final String RESOURCE_URI = "/api/articles";

    @Test
    @DisplayName("T01 - Post returns status of CREATED")
    public void test01(@Autowired MockMvc mockMvc)
        throws Exception {
        mockMvc.perform(post(RESOURCE_URI))
                .andExpect(status().isCreated());
    }
}
