package com.example.puppies.controller;

import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.security.JwtUtil;
import com.example.puppies.service.PostLikeService;
import com.example.puppies.service.PostService;
import com.example.puppies.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PuppiesController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

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
        return ResponseEntity.status(401).build();
    }

    @PostMapping(value = "/posts", consumes = {"multipart/form-data"})
    public ResponseEntity<PostEntity> createPost(
            @RequestParam("image") MultipartFile image,
            @RequestParam("content") String content,
            @RequestParam("date") LocalDateTime date,
            Authentication authentication) {

        String email = authentication.getName();


        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(email, content, date, image));
    }

    @GetMapping("/users/{id}/feed")
    public ResponseEntity<List<PostEntity>> getUserFeed(@PathVariable Long id) {
        return userService.findById(id)
                .map(userEntity -> ResponseEntity.ok(postService.getUserFeed(userEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostEntity> getPostDetails(@PathVariable Long postId) {
        return postService.getPostById(postId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userService.findByEmail(username);

        postLikeService.addLike(postId, user);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<UserEntity> fetchUserProfile(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/posts/liked")
    public ResponseEntity<List<PostEntity>> fetchLikedPosts(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userService.findByEmail(email);
        List<PostEntity> likedPosts = postService.getLikedPosts(user);
        return ResponseEntity.ok(likedPosts);
    }

    @GetMapping("/posts/made")
    public ResponseEntity<List<PostEntity>> fetchUserPosts(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userService.findByEmail(email);
        List<PostEntity> userPosts = postService.getUserPosts(user);
        return ResponseEntity.ok(userPosts);
    }
}
