package com.example.puppies.controller;

import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.service.PostLikeService;
import com.example.puppies.service.PostService;
import com.example.puppies.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

    @GetMapping("/users/{id}/feed")
    public ResponseEntity<List<PostEntity>> getUserFeed(@PathVariable Long id) {
        return userService.findById(id)
                .map(userEntity -> ResponseEntity.ok(postService.getUserFeed(userEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<UserEntity> fetchUserProfile(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
