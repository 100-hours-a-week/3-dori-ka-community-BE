package com.example.week7.service.post;

import com.example.week7.common.AuthValidator;
import com.example.week7.common.exception.custom.ResourceNotFoundException;
import com.example.week7.common.exception.custom.UnauthorizedException;
import com.example.week7.domain.Post;
import com.example.week7.domain.User;
import com.example.week7.dto.request.post.PostRequestDto;
import com.example.week7.dto.response.post.PostCreateResponse;
import com.example.week7.dto.response.post.PostDetailResponse;
import com.example.week7.dto.response.post.PostListResponse;
import com.example.week7.repository.post.PostRepository;
import com.example.week7.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.example.week7.common.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthValidator authValidator;
    private final PostViewService postViewService;

    @Override
    public PostCreateResponse createPost(PostRequestDto dto, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException(UNAUTHORIZED));
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
    public PostDetailResponse update(PostRequestDto dto, Long id, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

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
    public Page<PostListResponse> getAllPostByUser(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

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
