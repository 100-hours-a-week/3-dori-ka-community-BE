package com.example.community.service.post;

import com.example.community.domain.User;
import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.request.post.PostUpdateDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.dto.response.post.PostImageResponse;
import com.example.community.dto.response.post.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface PostService {

    PostCreateResponse createPost(PostRequestDto dto, User user);

    PostDetailResponse getPost(Long id);

    PostDetailResponse update(PostUpdateDto dto, Long id, User user);

    Page<PostListResponse> getAllPost(Pageable pageable);

    Page<PostListResponse> getAllPostByUser(User user, Pageable pageable);

//    Slice<PostListResponse> getAllPostSlice(Pageable pageable);

    List<PostImageResponse> getAllPostImageByPostId(Long id);

    void delete(Long id);
}
