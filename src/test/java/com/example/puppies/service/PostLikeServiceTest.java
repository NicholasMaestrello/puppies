package com.example.puppies.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.PostLikeEntity;
import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.PostLikeRepository;
import com.example.puppies.repository.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PostLikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    @InjectMocks
    private PostService postService;

    @Mock
    private AmazonS3 amazonS3;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddLike_Success() {
        Long postId = 1L;
        UserEntity user = new UserEntity();
        PostEntity post = new PostEntity();

        post.setId(postId);
        post.setContent("This is a test post.");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByUserAndPost(user, post)).thenReturn(false);
        postLikeService.addLike(postId, user);

        verify(postRepository, times(1)).findById(postId);
        verify(postLikeRepository, times(1)).existsByUserAndPost(user, post);
        verify(postLikeRepository, times(1)).save(any(PostLikeEntity.class));
    }

    @Test
    public void testAddLike_PostNotFound() {
        Long postId = 2L;
        UserEntity user = new UserEntity();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            postLikeService.addLike(postId, user);
        });

        Assertions.assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(postLikeRepository, never()).existsByUserAndPost(any(), any());
        verify(postLikeRepository, never()).save(any(PostLikeEntity.class));
    }

    @Test
    public void testAddLike_AlreadyLiked() {
        Long postId = 3L;
        UserEntity user = new UserEntity();
        PostEntity post = new PostEntity();

        post.setId(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByUserAndPost(user, post)).thenReturn(true);

        postLikeService.addLike(postId, user);

        verify(postRepository, times(1)).findById(postId);
        verify(postLikeRepository, times(1)).existsByUserAndPost(user, post);
        verify(postLikeRepository, never()).save(any(PostLikeEntity.class));
    }

    @Test
    public void testGetImageFromS3_Success() throws IOException {
        String imageUrl = "testImage.jpg";
        byte[] expectedBytes = "mockImageBytes".getBytes();
        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockInputStream = mock(S3ObjectInputStream.class);

        when(amazonS3.getObject("sample-bucket", imageUrl)).thenReturn(mockS3Object);
        when(mockS3Object.getObjectContent()).thenReturn(mockInputStream);

        when(mockInputStream.readAllBytes()).thenReturn(expectedBytes);

        byte[] imageBytes = postService.getImageFromS3(imageUrl);

        Assertions.assertArrayEquals(expectedBytes, imageBytes);
        verify(amazonS3, times(1)).getObject("sample-bucket", imageUrl);
    }


    @Test
    public void testGetUserFeed_Success() {
        UserEntity user = new UserEntity();
        PostEntity post1 = new PostEntity();
        post1.setId(1L);
        post1.setContent("Post 1");

        PostEntity post2 = new PostEntity();
        post2.setId(2L);
        post2.setContent("Post 2");

        when(postRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of(post1, post2));

        List<PostEntity> userFeed = postService.getUserFeed(user);

        Assertions.assertEquals(2, userFeed.size());
        Assertions.assertEquals("Post 1", userFeed.get(0).getContent());
        verify(postRepository, times(1)).findByUserOrderByDateDesc(user);
    }

    @Test
    public void testGetPostById_Found() {
        Long postId = 1L;
        PostEntity post = new PostEntity();
        post.setId(postId);
        post.setContent("Post content");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Optional<PostEntity> foundPost = postService.getPostById(postId);

        Assertions.assertTrue(foundPost.isPresent());
        Assertions.assertEquals(postId, foundPost.get().getId());
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    public void testGetPostById_NotFound() {
        Long postId = 2L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        Optional<PostEntity> foundPost = postService.getPostById(postId);

        Assertions.assertFalse(foundPost.isPresent());
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    public void testGetUserPosts_Success() {
        UserEntity user = new UserEntity();
        PostEntity post1 = new PostEntity();
        post1.setId(1L);
        post1.setContent("User's Post 1");

        PostEntity post2 = new PostEntity();
        post2.setId(2L);
        post2.setContent("User's Post 2");

        when(postRepository.findByUser(user)).thenReturn(List.of(post1, post2));

        List<PostEntity> userPosts = postService.getUserPosts(user);

        Assertions.assertEquals(2, userPosts.size());
        Assertions.assertEquals("User's Post 1", userPosts.get(0).getContent());
        verify(postRepository, times(1)).findByUser(user);
    }

    @Test
    public void testGetLikedPosts_Success() {
        UserEntity user = new UserEntity();
        user.setId(1L);

        PostEntity post1 = new PostEntity();
        post1.setId(1L);
        post1.setContent("Liked Post 1");

        PostEntity post2 = new PostEntity();
        post2.setId(2L);
        post2.setContent("Liked Post 2");

        PostLikeEntity like1 = new PostLikeEntity();
        like1.setPost(post1);
        like1.setUser(user);

        PostLikeEntity like2 = new PostLikeEntity();
        like2.setPost(post2);
        like2.setUser(user);

        when(postLikeRepository.findByUser(user)).thenReturn(List.of(like1, like2));

        List<PostEntity> likedPosts = postService.getLikedPosts(user);

        Assertions.assertEquals(2, likedPosts.size());
        Assertions.assertEquals("Liked Post 1", likedPosts.get(0).getContent());
        Assertions.assertEquals("Liked Post 2", likedPosts.get(1).getContent());

        verify(postLikeRepository, times(1)).findByUser(user);
    }

    @Test
    public void testGetLikedPosts_NoLikes() {
        UserEntity user = new UserEntity();
        user.setId(1L);

        when(postLikeRepository.findByUser(user)).thenReturn(List.of());

        List<PostEntity> likedPosts = postService.getLikedPosts(user);

        Assertions.assertEquals(0, likedPosts.size());

        verify(postLikeRepository, times(1)).findByUser(user);
    }
}
