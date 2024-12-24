package com.example.puppies.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostDto implements Serializable {

    private String imageUrl;
    private String content;
    private LocalDateTime date;
}
