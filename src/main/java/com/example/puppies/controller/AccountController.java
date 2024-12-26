package com.example.puppies.controller;

import com.example.puppies.entity.UserEntity;
import com.example.puppies.security.JwtUtil;
import com.example.puppies.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private UserService userService;

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

}
