package com.example.community.service.comment;

import com.example.community.domain.User;
import com.example.community.dto.request.comment.CommentRequestDto;
import com.example.community.dto.response.comment.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse createComment(CommentRequestDto dto, Long postId, User user);

    Page<CommentResponse> getCommentByPost(Long postId, Pageable pageable);

    Page<CommentResponse> getCommentByUser(User user, Pageable pageable);

    CommentResponse getComment(Long id) ;

    CommentResponse update(CommentRequestDto dto, Long id, User user);

    void delete(Long id);
}
