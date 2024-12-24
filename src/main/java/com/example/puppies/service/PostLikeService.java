package com.example.puppies.service;

import com.example.puppies.entity.PostLikeEntity;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostLikeRepository;
import com.example.puppies.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostLikeService {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostRepository postRepository;

    public void addLike(Long postId, UserEntity user) {
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!postLikeRepository.existsByUserAndPost(user, post)) {
            PostLikeEntity like = new PostLikeEntity();
            like.setPost(post);
            like.setUser(user);
            postLikeRepository.save(like);
        }
    }

}
