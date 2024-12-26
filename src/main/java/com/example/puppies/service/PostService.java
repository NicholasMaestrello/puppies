package com.example.puppies.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.PostLikeEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostLikeRepository;
import com.example.puppies.repository.PostRepository;
import com.example.puppies.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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
    private AmazonS3 amazonS3;

    @Autowired
    private CacheManager cacheManager;


    @Autowired
    private PostLikeRepository postLikeRepository;

    @Value("${puppies.bucket.name}")
    private String bucketName;

    public PostEntity createPost(String email, String  content, LocalDateTime date, MultipartFile image) throws IOException {
        UserEntity user = userRepository.findByEmail(email);

        var entity = savePostEntity(content, date, image, user);

        cacheManager.getCache("userFeedCache").evict(user.getId());
        cacheManager.getCache("userPostsCache").evict(user.getId());
        return entity;
    }

    public PostEntity savePostEntity(String content, LocalDateTime date, MultipartFile image, UserEntity user) throws IOException {
        String fileName = saveImageOnS3(image);

        PostEntity postEntity = new PostEntity();
        postEntity.setImageUrl(fileName);
        postEntity.setContent(content);
        postEntity.setDate(date);
        postEntity.setUser(user);

        return postRepository.save(postEntity);
    }

    private String saveImageOnS3(MultipartFile image) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(image.getSize());

        try (InputStream inputStream = image.getInputStream()) {
            amazonS3.putObject("sample-bucket", fileName, inputStream, metadata);
        }
        return fileName;
    }

    public byte[] getImageFromS3(String imageUrl) {
        S3Object s3Object = amazonS3.getObject("sample-bucket", imageUrl);
        try (InputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving image from S3", e);
        }
    }

    @Cacheable(value = "userFeedCache", key = "#user.id")
    public List<PostEntity> getUserFeed(UserEntity user) {
        return postRepository.findByUserOrderByDateDesc(user);
    }

    public Optional<PostEntity> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    @Cacheable(value = "userPostsCache", key = "#user.id")
    public List<PostEntity> getUserPosts(UserEntity user) {
        return postRepository.findByUser(user);
    }

    @Cacheable(value = "likedPostsCache", key = "#user.id")
    public List<PostEntity> getLikedPosts(UserEntity user) {
        List<PostLikeEntity> likes = postLikeRepository.findByUser(user);
        return likes.stream()
                .map(PostLikeEntity::getPost)
                .collect(Collectors.toList());
    }
}
