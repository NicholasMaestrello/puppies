package com.example.puppies.controller;

import com.example.puppies.dto.PostDto;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.security.JwtUtil;
import com.example.puppies.service.PostService;
import com.example.puppies.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PuppiesController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/account")
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PostMapping("/account/authentication")
    public ResponseEntity<String> authenticateUser(@RequestBody UserEntity user) {
        UserEntity authenticatedUser = userService.authenticateUser(user.getEmail());
        if (authenticatedUser != null) {
            String token = jwtUtil.generateToken(authenticatedUser.getEmail());
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).build(); // Unauthorized
    }

    @PostMapping("/posts")
    public ResponseEntity<PostEntity> createPost(@RequestBody PostDto post, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(postService.createPost(post, email));
    }

    @GetMapping("/users/{id}/feed")
    public ResponseEntity<List<PostEntity>> getUserFeed(@PathVariable Long id) {
        return userService.findById(id)
                .map(userEntity -> ResponseEntity.ok(postService.getUserFeed(userEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
