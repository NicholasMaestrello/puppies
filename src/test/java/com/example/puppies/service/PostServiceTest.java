package com.example.puppies.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostLikeRepository;
import com.example.puppies.repository.PostRepository;
import com.example.puppies.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private PostService postService;

    @Value("${puppies.bucket.name}")
    private String bucketName = "sample-bucket";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePost_Success() throws IOException {
        String email = "user@example.com";
        String content = "This is a new post.";
        LocalDateTime date = LocalDateTime.now();

        UserEntity user = new UserEntity();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        when(image.getOriginalFilename()).thenReturn("testImage.jpg");
        when(image.getSize()).thenReturn(1024L);
        when(image.getInputStream()).thenReturn(mock(InputStream.class));

        when(amazonS3.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                .thenReturn(null);

        PostEntity postEntity = postService.createPost(email, content, date, image);

        verify(userRepository, times(1)).findByEmail(email);
        verify(postRepository, times(1)).save(any(PostEntity.class));

        Assertions.assertNotNull(postEntity);
        Assertions.assertTrue( postEntity.getImageUrl().endsWith("testImage.jpg"));
        Assertions.assertEquals(content, postEntity.getContent());
        Assertions.assertEquals(date, postEntity.getDate());
    }

    @Test
    public void testCreatePost_ImageUploadFails() throws IOException {
        String email = "user@example.com";
        String content = "This is a new post.";
        LocalDateTime date = LocalDateTime.now();

        UserEntity user = new UserEntity();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(image.getOriginalFilename()).thenReturn("testImage.jpg");
        when(image.getSize()).thenReturn(1024L);

        when(image.getInputStream()).thenThrow(new IOException("Image upload failed"));

        try {
            postService.createPost(email, content, date, image);
        } catch (IOException e) {
            assert e.getMessage().contains("Image upload failed");
        }

        verify(userRepository, times(1)).findByEmail(email);
        verify(postRepository, never()).save(any(PostEntity.class));
    }
}
