package com.example.puppies;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostResponseDTO implements Serializable {
    private Long id;
    private String content;
    private LocalDateTime date;
    private byte[] image;
}
