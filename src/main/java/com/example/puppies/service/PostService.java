package com.example.puppies.service;

import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.PostLikeEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostLikeRepository;
import com.example.puppies.repository.PostRepository;
import com.example.puppies.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    public PostEntity createPost(String email, String  content, LocalDateTime date, MultipartFile image) {
        UserEntity user = userRepository.findByEmail(email);

        PostEntity postEntity = new PostEntity();
        postEntity.setImageUrl(saveImage(image));
        postEntity.setContent(content);
        postEntity.setDate(date);
        postEntity.setUser(user);

        return postRepository.save(postEntity);
    }

    public List<PostEntity> getUserFeed(UserEntity user) {
        return postRepository.findByUserOrderByDateDesc(user);
    }

    public Optional<PostEntity> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    public List<PostEntity> getUserPosts(UserEntity user) {
        return postRepository.findByUser(user);
    }

    public List<PostEntity> getLikedPosts(UserEntity user) {
        List<PostLikeEntity> likes = postLikeRepository.findByUser(user);
        return likes.stream()
                .map(PostLikeEntity::getPost)
                .collect(Collectors.toList());
    }

    private String saveImage(MultipartFile image) {
        return "URL/to/saved/image";
    }
}
