package com.example.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ProfileImage {

    @Id
    @Column(name = "PROFILE_IMAGE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String profileImageUrl;

    public ProfileImage(String fileName, String profileImageUrl) {
        this.fileName = fileName;
        this.profileImageUrl = profileImageUrl;
    }
}
