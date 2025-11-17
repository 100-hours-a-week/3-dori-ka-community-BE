package com.example.community.service.post;

import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.dto.response.post.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostService {

    PostCreateResponse createPost(PostRequestDto dto, String email);

    PostDetailResponse getPost(Long id);

    PostDetailResponse update(PostRequestDto dto, Long id, String email);

    Page<PostListResponse> getAllPost(Pageable pageable);

    Page<PostListResponse> getAllPostByUser(String email, Pageable pageable);

    Slice<PostListResponse> getAllPostSlice(Pageable pageable);

    void delete(Long id);
}
