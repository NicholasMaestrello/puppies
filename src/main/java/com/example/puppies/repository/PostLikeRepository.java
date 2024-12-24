package com.example.puppies.repository;

import com.example.puppies.entity.PostLikeEntity;
import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {
    boolean existsByUserAndPost(UserEntity user, PostEntity post);
    List<PostLikeEntity> findByUser(UserEntity user);
}
