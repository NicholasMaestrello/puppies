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

public class UserControllerTest {

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
    private UserController userController;

    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        this.objectMapper = new ObjectMapper();
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
}
