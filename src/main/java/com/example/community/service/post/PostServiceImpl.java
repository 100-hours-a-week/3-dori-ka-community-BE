package com.example.community.service.post;

import com.example.community.common.util.AuthValidator;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.dto.response.post.PostListResponse;
import com.example.community.repository.post.PostRepository;
import com.example.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.example.community.common.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final AuthValidator authValidator;
    private final PostViewService postViewService;

    @Override
    public PostCreateResponse createPost(PostRequestDto dto, User user) {

        Post post = PostRequestDto.ofEntity(dto);

        post.setMappingUser(user);

        Post savedPost = postRepository.save(post);

        return PostCreateResponse.fromEntity(savedPost);
    }

    @Override
    public PostDetailResponse getPost(Long id) {
        Post post = postRepository
                .findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));
        postViewService.increaseViewcount(id);
        return PostDetailResponse.fromEntity(post);
    }

    @Override
    public PostDetailResponse update(PostRequestDto dto, Long id, User user) {

        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );
        authValidator.validate(user, post.getUser());

        post.update(dto.getTitle(), dto.getContent());

        return PostDetailResponse.fromEntity(post);
    }


    @Override
    public Page<PostListResponse> getAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAllWithUser(pageable);
        return posts.map(PostListResponse::fromEntity);
    }

    @Override
    public Page<PostListResponse> getAllPostByUser(User user, Pageable pageable) {
        Page<Post> posts = postRepository.findAllByUser(user, pageable);

        return posts.map(PostListResponse::fromEntity);
    }

    @Override
    public Slice<PostListResponse> getAllPostSlice(Pageable pageable) {
        Slice<Post> posts = postRepository.findAllWithUserSlice(pageable);

        return posts.map(PostListResponse::fromEntity);
    }

    @Override
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}
