package com.example.puppies.repository;

import com.example.puppies.entity.PostEntity;
import com.example.puppies.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository  extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByUserOrderByDateDesc(UserEntity user);
}
