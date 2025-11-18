package com.example.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PostImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID")
    private Post post;

    private String postImageUrl;

    public PostImage(Post post, String fileName, String postImageUrl) {
        this.post = post;
        this.fileName = fileName;
        this.postImageUrl = postImageUrl;
    }
}
