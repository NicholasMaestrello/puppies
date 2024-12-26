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


public class AccountControllerTest {


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
    private AccountController userController;

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
}
