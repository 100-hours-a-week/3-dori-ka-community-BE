package com.example.community.service.post;

import com.example.community.common.exception.custom.ForbiddenException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.common.util.AuthValidator;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.domain.Post;
import com.example.community.domain.PostImage;
import com.example.community.domain.User;
import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.request.post.PostUpdateDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.dto.response.post.PostImageResponse;
import com.example.community.dto.response.post.PostListResponse;
import com.example.community.repository.post.PostLikeRepository;
import com.example.community.repository.post.PostRepository;
import com.example.community.repository.post.PostImageRepository;
import com.example.community.repository.user.UserRepository;
import com.example.community.service.post.viewcount.PostViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;


import java.util.List;

import static com.example.community.common.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    private final PostViewService postViewService;
    private final AuthValidator authValidator;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;


    @Override
    public PostCreateResponse createPost(PostRequestDto dto, User user) {

        if (user == null || user.getId() == null) {
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        User loginUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

        Post post = PostRequestDto.ofEntity(dto);

        post.setMappingUser(loginUser);

        Post savedPost = postRepository.save(post);

        if (dto.getPostImageUrls() != null && !dto.getPostImageUrls().isEmpty()) {
            for (String imageUrl : dto.getPostImageUrls()) {
                PostImage postImage = PostImage.builder()
                        .post(savedPost)
                        .postImageUrl(imageUrl)
                        .build();
                postImageRepository.save(postImage);
                savedPost.addPostImages(postImage);
            }
        }

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
    public PostDetailResponse update(PostUpdateDto dto, Long id, User user) {

        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );

        authValidator.validate(user, post.getUser());

        post.update(dto.getTitle(), dto.getContent());

        if (dto.getDeletedImageIds() != null && !dto.getDeletedImageIds().isEmpty()) {

            for (Long imageId : dto.getDeletedImageIds()) {

                PostImage postImage = postImageRepository.findById(imageId)
                        .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));

                if (!postImage.getPost().getId().equals(post.getId())) {
                    throw new ForbiddenException(FORBIDDEN);
                }

                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(postImage.getPostImageUrl())
                        .build());

                postImageRepository.delete(postImage);

                // 엔티티 관계에서도 제거
                post.getPostImages().remove(postImage);
            }
        }

        if (dto.getNewPostImageUrls() != null && !dto.getNewPostImageUrls().isEmpty()) {

            for (String imageUrl : dto.getNewPostImageUrls()) {

                PostImage newImage = PostImage.builder()
                        .post(post)
                        .postImageUrl(imageUrl)
                        .build();

                postImageRepository.save(newImage);

                post.addPostImages(newImage);
            }
        }

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

/*    @Override
    public Slice<PostListResponse> getAllPostSlice(Pageable pageable) {
        Slice<Post> posts = postRepository.findAllWithUserSlice(pageable);

        return posts.map(PostListResponse::fromEntity);
    }*/

    @Override
    public List<PostImageResponse> getAllPostImageByPostId(Long id) {
        List<PostImage> postImages = postImageRepository.findAllByPostId(id);

        return postImages.stream().map(PostImageResponse::fromEntity).toList();
    }

    @Override
    public void delete(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );

        List<PostImage> postImages = postImageRepository.findAllByPostId(post.getId());
        for (PostImage postImage : postImages) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                            .key(postImage.getPostImageUrl()).build());
        }

        postLikeRepository.deleteAllByPostId(post.getId());

        postRepository.delete(post);
    }
}
