package com.example.week7.service.comment;

import com.example.week7.domain.User;
import com.example.week7.dto.request.comment.CommentRequestDto;
import com.example.week7.dto.response.comment.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(CommentRequestDto dto, Long postId, String email);

    Page<CommentResponse> getCommentByPost(Long postId, Pageable pageable);

    Page<CommentResponse> getCommentByUser(String email, Pageable pageable);

    CommentResponse getComment(Long id) ;

    CommentResponse update(CommentRequestDto dto, Long id, String email);

    void delete(Long id);
}
