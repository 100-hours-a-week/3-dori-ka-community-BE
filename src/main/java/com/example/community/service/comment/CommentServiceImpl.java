package com.example.community.service.comment;

import com.example.community.common.AuthValidator;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.domain.Comment;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.dto.request.comment.CommentRequestDto;
import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.repository.comment.CommentRepository;
import com.example.community.repository.post.PostRepository;
import com.example.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.community.common.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AuthValidator authValidator;

    @Override
    public CommentResponse createComment(CommentRequestDto dto, Long postId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );

        Comment comment = CommentRequestDto.ofEntity(dto);
        comment.setMappingUser(user);
        comment.setMappingPost(post);

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.fromEntity(savedComment);
    }

    @Override
    public Page<CommentResponse> getCommentByPost(Long postId, Pageable pageable) {

        Page<Comment> comments = commentRepository.findAllByPostIdWithUser(postId, pageable);
        return comments.map(CommentResponse::fromEntity);
    }

    @Override
    public Page<CommentResponse> getCommentByUser(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

        Page<Comment> comments = commentRepository.findAllByUser(user, pageable);
        return comments.map(CommentResponse::fromEntity);
    }

    @Override
    public CommentResponse getComment(Long id) {

        Comment comment = commentRepository.findByIdWithUser(id).orElseThrow(()
                -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        return CommentResponse.fromEntity(comment);
    }

    @Override
    public CommentResponse update(CommentRequestDto dto, Long id, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

        Comment comment = commentRepository.findByIdWithUser(id).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );
        authValidator.validate(user, comment.getUser());

        comment.update(dto.getContent());
        return CommentResponse.fromEntity(comment);
    }

    @Override
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }
}
