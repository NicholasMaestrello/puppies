package com.example.puppies.repository;

import com.example.puppies.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
}
