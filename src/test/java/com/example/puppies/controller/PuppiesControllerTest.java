package com.example.puppies.controller;

import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.security.JwtUtil;
import com.example.puppies.service.PostLikeService;
import com.example.puppies.service.PostService;
import com.example.puppies.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class PuppiesControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;



    @InjectMocks
    private PuppiesController puppiesController;

    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(puppiesController).build();
        this.objectMapper = new ObjectMapper();
    }


    @Test
    public void testCreateUser_Success() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setName("User One");

        when(userService.createUser(any(UserEntity.class))).thenReturn(user);

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("User One"));
    }

    @Test
    public void testAuthenticateUser_Success() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");
        user.setName("User One");

        String token = "generated-token";

        when(userService.authenticateUser(user.getEmail())).thenReturn(user);
        when(jwtUtil.generateToken(user.getEmail())).thenReturn(token);

        mockMvc.perform(post("/api/account/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(token));
    }

    @Test
    public void testAuthenticateUser_Failure() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("nonexistent@example.com");

        when(userService.authenticateUser(user.getEmail())).thenReturn(null);

        mockMvc.perform(post("/api/account/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                "cat01.jpg",
                "image/jpeg",
                "mockImageContent".getBytes()
        );

        String content = "This is a new post";
        LocalDateTime date = LocalDateTime.parse("2024-10-05T00:00:00");
        String email = "user@example.com";

        PostEntity postDto = new PostEntity();
        postDto.setId(1L);
        postDto.setContent(content);
        postDto.setDate(date);
        postDto.setImageUrl("image-url");

        when(authentication.getName()).thenReturn(email);
        when(postService.createPost(anyString(), anyString(), any(LocalDateTime.class), any(MultipartFile.class)))
                .thenReturn(postDto);


        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.POST, "/api/posts")
                        .file(mockFile)
                        .param("content", content)
                        .param("date", "2024-10-05T00:00:00")
                        .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value(content));

        var localDateTimeArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(postService, times(1)).createPost(eq(email), eq(content), localDateTimeArgumentCaptor.capture(), any(MultipartFile.class));
        Assertions.assertTrue(localDateTimeArgumentCaptor.getValue().isEqual(date));
    }

    @Test
    public void testCreatePost_Error() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                "cat01.jpg",
                "image/jpeg",
                "mockImageContent".getBytes()
        );

        String content = "This is a new post.";
        LocalDateTime date = LocalDateTime.now();

        when(authentication.getName()).thenReturn("user@example.com");

        doThrow(new IOException("Failed to upload image"))
                .when(postService).createPost(any(String.class), any(String.class), any(LocalDateTime.class), any(MultipartFile.class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/posts")
                        .file(mockFile)
                        .param("content", content)
                        .param("date", date.toString())
                        .principal(authentication))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetUserFeed_Success() throws Exception {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(postService.getUserFeed(user)).thenReturn(List.of(new PostEntity()));

        mockMvc.perform(get("/api/users/{id}/feed", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(userService, times(1)).findById(userId);
        verify(postService, times(1)).getUserFeed(user);
    }

    @Test
    public void testLikePost_Success() throws Exception {
        Long postId = 1L;
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);
        doNothing().when(postLikeService).addLike(postId, user);

        mockMvc.perform(post("/api/posts/{postId}/like", postId)
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(postLikeService, times(1)).addLike(postId, user);
    }

    @Test
    public void testFetchUserProfile_Success() throws Exception {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("user@example.com");

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{userId}/profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    public void testFetchLikedPosts_Success() throws Exception {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(email);
        List<PostEntity> likedPosts = List.of(new PostEntity());

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);
        when(postService.getLikedPosts(user)).thenReturn(likedPosts);

        mockMvc.perform(get("/api/posts/liked")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(postService, times(1)).getLikedPosts(user);
    }

    @Test
    public void testFetchUserPosts_Success() throws Exception {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(email);

        List<PostEntity> userPosts = List.of(new PostEntity(), new PostEntity());

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);
        when(postService.getUserPosts(user)).thenReturn(userPosts);

        mockMvc.perform(get("/api/posts/made")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(userService, times(1)).findByEmail(email);
        verify(postService, times(1)).getUserPosts(user);
    }

    @Test
    public void testGetPostDetails_Success() throws Exception {
        Long postId = 1L;
        PostEntity post = new PostEntity();
        post.setId(postId);
        post.setContent("This is a test post.");
        post.setImageUrl("test-image-url");
        post.setDate(LocalDateTime.now());

        when(postService.getPostById(postId)).thenReturn(Optional.of(post));
        when(postService.getImageFromS3(post.getImageUrl())).thenReturn(new byte[]{ /* byte array content */ });

        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.content").value("This is a test post."));

        verify(postService, times(1)).getPostById(postId);
        verify(postService, times(1)).getImageFromS3(post.getImageUrl());
    }

    @Test
    public void testGetPostDetails_NotFound() throws Exception {
        Long postId = 1L;

        when(postService.getPostById(postId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getPostById(postId);
    }
}
