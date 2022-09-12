package com.spankinfresh.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.spankinfresh.blog.data.BlogPostRepository;
import com.spankinfresh.blog.domain.Author;
import com.spankinfresh.blog.domain.BlogPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BlogPostControllerTests {

    @MockBean
    private BlogPostRepository mockRepository;
    private static final String RESOURCE_URI = "/api/articles";
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Author savedAuthor = new Author(1L, "Jane", "Doe", "jane@doe.com");
    private static final BlogPost testPosting = new BlogPost(0L, "category", null, "title", "content", savedAuthor);
    private static final BlogPost savedPosting = new BlogPost(1l, "category", LocalDateTime.now(), "title", "content", savedAuthor);

    @Test
    @DisplayName("T01 - POST accepts and returns blog post representation")
    public void postCreatesNewBlogEntry_Test(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.save(refEq(testPosting, "datePosted", "author"))).thenReturn(savedPosting);
        MvcResult result = mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testPosting)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedPosting.getId()))
                .andExpect(jsonPath("$.title").value(savedPosting.getTitle()))
                .andExpect(jsonPath("$.datePosted").value(savedPosting.getDatePosted().toString().substring(0, savedPosting.getDatePosted().toString().length() - 2)))
                .andExpect(jsonPath("$.category").value(savedPosting.getCategory()))
                .andExpect(jsonPath("$.content").value(savedPosting.getContent()))
                .andExpect(jsonPath("$.author.id").value(savedPosting.getAuthor().getId()))
                .andReturn();
        MockHttpServletResponse mockResponse = result.getResponse();
        assertEquals(String.format("http://localhost/api/articles/%d", savedPosting.getId()), mockResponse.getHeader("Location"));
        verify(mockRepository, times(1)).save(refEq(testPosting, "datePosted", "author"));
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T02 - When no articles exist, GET returns an empty list")
    public void test_02(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findAll()).thenReturn(new ArrayList<BlogPost>());
        mockMvc.perform(get(RESOURCE_URI))
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findAll();
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T03 - When one article exists, GET returns a list with it")
    public void test_03(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findAll()).thenReturn(Collections.singletonList(savedPosting));
        mockMvc.perform(get(RESOURCE_URI))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(savedPosting.getId()))
                .andExpect(jsonPath("$.[0].title").value(savedPosting.getTitle()))
                .andExpect(jsonPath("$.[0].datePosted").value(savedPosting.getDatePosted().toString().substring(0, savedPosting.getDatePosted().toString().length() - 2)))
                .andExpect(jsonPath("$.[0].category").value(savedPosting.getCategory()))
                .andExpect(jsonPath("$.[0].content").value(savedPosting.getContent()))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findAll();
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T04 - Requested article does not exist so GET returns 404")
    public void test_04(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(get(RESOURCE_URI + "/1")).andExpect(status().isNotFound());
        verify(mockRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T05 - Requested article exists so GET returns it in a list")
    public void test_05(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(anyLong())).thenReturn(Optional.of(savedPosting));
        mockMvc.perform(get(RESOURCE_URI + "/1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(savedPosting.getId()))
                .andExpect(jsonPath("$.[0].title").value(savedPosting.getTitle()))
                .andExpect(jsonPath("$.[0].datePosted").value(savedPosting.getDatePosted().toString().substring(0, savedPosting.getDatePosted().toString().length() - 2)))
                .andExpect(jsonPath("$.[0].category").value(savedPosting.getCategory()))
                .andExpect(jsonPath("$.[0].content").value(savedPosting.getContent()))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T06 - Article to be updated does not exist so PUT returns 404")
    public void test_06(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.existsById(10L)).thenReturn(false);
        mockMvc.perform(put(RESOURCE_URI + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BlogPost(10L, "category", null, "title", "content", savedAuthor))))
                .andExpect(status().isNotFound());
        verify(mockRepository, never()).save(any(BlogPost.class));
        verify(mockRepository, times(1)).existsById(10L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T07 - Article to be updated exists so PUT saves new copy")
    public void test_07(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.existsById(10L)).thenReturn(true);
        mockMvc.perform(put(RESOURCE_URI + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BlogPost(10L, "category", null, "title", "content", savedAuthor))))
                .andExpect(status().isNoContent());
        verify(mockRepository, times(1)).save(any(BlogPost.class));
        verify(mockRepository, times(1)).existsById(10L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T08 - ID in PUT URL not equal to one in request body")
    public void test_08(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(put(RESOURCE_URI + "/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BlogPost(10L, "category", null, "title", "content", savedAuthor))))
                .andExpect(status().isConflict());
        verify(mockRepository, never()).save(any(BlogPost.class));
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T09 - Article to be removed does not exist so DELETE returns 404")
    public void test_09(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(delete(RESOURCE_URI + "/1"))
                .andExpect(status().isNotFound());
        verify(mockRepository, never()).delete(any(BlogPost.class));
        verify(mockRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T10 - Article to be removed exists so DELETE deletes it")
    public void test_10(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(1L)).thenReturn(Optional.of(savedPosting));
        mockMvc.perform(delete(RESOURCE_URI + "/1"))
                .andExpect(status().isNoContent());
        verify(mockRepository, times(1)).delete(refEq(savedPosting));
        verify(mockRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T11 - POST returns 400 if required properties are not set")
    public void test_11(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BlogPost())))
                .andExpect(status().isBadRequest());
        verify(mockRepository, never()).save(any(BlogPost.class));
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T12 - Field errors present for each invalid property")
    public void test_12(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BlogPost())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").value("must not be null"))
                .andExpect(jsonPath("$.fieldErrors.title").value("must not be null"))
                .andExpect(jsonPath("$.fieldErrors.content").value("must not be null"));
        mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BlogPost(0L, "", null, "", "", savedAuthor))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").value("Please enter a category name of up to 200 characters"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Please enter a title up to 200 characters in length"))
                .andExpect(jsonPath("$.fieldErrors.content").value("Content is required"));
        verify(mockRepository, never()).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("T13 - Get requests have proper CORS headers")
    public void test_13(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findAll()).thenReturn(Collections.singletonList(savedPosting));
        mockMvc.perform(get(RESOURCE_URI))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.VARY,
                        hasItems("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")));
    }

    @Test
    @DisplayName("T14 - Get by category name returns expected data")
    public void test_14(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findByCategoryOrderByDatePostedDesc("foo")).thenReturn(Collections.singletonList(savedPosting));
        mockMvc.perform(get(RESOURCE_URI + "/category")
                        .param("categoryName", "foo"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(savedPosting.getId()))
                .andExpect(jsonPath("$.[0].title").value(savedPosting.getTitle()))
                .andExpect(jsonPath("$.[0].datePosted").value(savedPosting.getDatePosted().toString().substring(0, savedPosting.getDatePosted().toString().length() - 2)))
                .andExpect(jsonPath("$.[0].category").value(savedPosting.getCategory()))
                .andExpect(jsonPath("$.[0].content").value(savedPosting.getContent()))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findByCategoryOrderByDatePostedDesc("foo");
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T15 - Get by category without a category = bad request")
    public void test_15(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(get(RESOURCE_URI + "/category"))
                .andExpect(status().isBadRequest());
        verify(mockRepository, never()).findByCategoryOrderByDatePostedDesc(anyString());
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("ST01: POST without JWT is forbidden")
    public void sTest01(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.save(any(BlogPost.class))).thenReturn(savedPosting);
        mockMvc.perform(post(RESOURCE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testPosting)))
                .andExpect(status().isForbidden());
        verify(mockRepository, never()).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("ST02: PUT without JWT is forbidden")
    public void sTest02(@Autowired MockMvc mockMvc) throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        when(mockRepository.existsById(anyLong())).thenReturn(true);
        mockMvc.perform(put(RESOURCE_URI + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(savedPosting)))
                .andExpect(status().isForbidden());
        verify(mockRepository, never()).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("ST03: DELETE without JWT is forbidden")
    public void sTest03(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(1L)).thenReturn(Optional.of(savedPosting));
        mockMvc.perform(delete(RESOURCE_URI + "/1"))
                .andExpect(status().isForbidden());
        verify(mockRepository, never()).delete(any(BlogPost.class));
    }
}
