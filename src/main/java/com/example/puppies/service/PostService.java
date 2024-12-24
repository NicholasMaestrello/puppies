package com.example.puppies.service;

import com.example.puppies.dto.PostDto;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostRepository;
import com.example.puppies.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public PostEntity createPost(PostDto post, String email) {
        UserEntity user = userRepository.findByEmail(email);

        PostEntity postEntity = new PostEntity();
        postEntity.setImageUrl(post.getImageUrl());
        postEntity.setContent(post.getContent());
        postEntity.setDate(post.getDate());
        postEntity.setUser(user);

        return postRepository.save(postEntity);
    }

    public List<PostEntity> getUserFeed(UserEntity user) {
        return postRepository.findByUserOrderByDateDesc(user);
    }
}
