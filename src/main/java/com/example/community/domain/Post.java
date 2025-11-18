package com.example.community.domain;

import com.example.community.common.BasicTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Post extends BasicTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    private Long viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    public Post(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.viewCount = 0L;
        this.user = user;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }


    public void setMappingUser(User user) {
        this.user = user;
    }


    public void deleteComment(Comment comment) {
        comments.remove(comment);
        comment.setMappingPost(null);
    }

    public void upViewcount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
