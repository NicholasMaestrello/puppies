package com.example.puppies.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class PostEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @ManyToOne
    private UserEntity user;

    @OneToMany(mappedBy = "post")
    @JsonIgnore
    private List<PostLikeEntity> likes;

    @Formula("(SELECT COUNT(l.id) FROM post_like l WHERE l.post_id = id)")
    private Long likeCount;
}
