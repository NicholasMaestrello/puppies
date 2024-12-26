package com.example.puppies.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.PostLikeEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostLikeRepository;
import com.example.puppies.repository.PostRepository;
import com.example.puppies.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Value("${puppies.bucket.name}")
    private String bucketName;

    public PostEntity createPost(String email, String  content, LocalDateTime date, MultipartFile image) throws IOException {
        UserEntity user = userRepository.findByEmail(email);

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.getSize());

            try (InputStream inputStream = image.getInputStream()) {
                var x = amazonS3.putObject("sample-bucket", fileName, inputStream, metadata);
                System.out.println(x.getVersionId());
            }

        PostEntity postEntity = new PostEntity();
        postEntity.setImageUrl(fileName);
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
