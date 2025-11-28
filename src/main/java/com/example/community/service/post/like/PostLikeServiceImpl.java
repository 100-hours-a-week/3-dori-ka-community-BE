package com.example.community.service.post.like;

import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.domain.Post;
import com.example.community.domain.PostLike;
import com.example.community.domain.User;
import com.example.community.dto.response.post.PostLikeResponse;
import com.example.community.repository.post.PostLikeRepository;
import com.example.community.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.community.common.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService{

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;


    @Override
    public PostLikeResponse addLike(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );

        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            throw new DuplicatedException(DUPLICATED_LIKES);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user).build();

        postLikeRepository.save(postLike);

        long likeCount = postLikeRepository.countByPostId(postId);

        return PostLikeResponse.fromEntity(postLike, true, likeCount);
    }

    @Override
    public PostLikeResponse removeLike(Long postId, User user) {
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                .orElseThrow(() -> new BadRequestException(NOT_LIKED_POST));

        postLikeRepository.delete(like);

        long likeCount = postLikeRepository.countByPostId(postId);

        return new PostLikeResponse(postId, false, likeCount);    }

    @Override
    @Transactional(readOnly = true)
    public PostLikeResponse getLikeCount(Long postId, User user) {
        Boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
        long likeCount = postLikeRepository.countByPostId(postId);
        return PostLikeResponse.builder()
                .postId(postId)
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}