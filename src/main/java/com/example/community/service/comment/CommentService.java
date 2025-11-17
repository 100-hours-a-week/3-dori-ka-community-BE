package com.example.community.service.comment;

import com.example.community.dto.request.comment.CommentRequestDto;
import com.example.community.dto.response.comment.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse createComment(CommentRequestDto dto, Long postId, String email);

    Page<CommentResponse> getCommentByPost(Long postId, Pageable pageable);

    Page<CommentResponse> getCommentByUser(String email, Pageable pageable);

    CommentResponse getComment(Long id) ;

    CommentResponse update(CommentRequestDto dto, Long id, String email);

    void delete(Long id);
}
