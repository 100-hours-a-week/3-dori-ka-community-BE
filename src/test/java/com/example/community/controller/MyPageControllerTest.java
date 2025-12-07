package com.example.community.controller;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.domain.User;
import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.dto.response.post.PostListResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.comment.CommentService;
import com.example.community.service.post.PostService;
import com.example.community.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.community.common.exception.ErrorMessage.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MyPageControllerTest {

    private static final String BASE_URL = "/users/me";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("마이페이지 사용자 정보 조회 - 성공")
    void get_user_info_success() throws Exception {

        UserDetailResponse response = createUserDetailResponse();

        when(userService.getUserInfo(any()))
                .thenReturn(response);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 조회 성공"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.createdDate").value("2024-01-01"))
                .andExpect(jsonPath("$.data.profileImage").value("profile.png"));

        verify(userService).getUserInfo(any());
    }

    @Test
    @DisplayName("마이페이지 사용자 게시글 목록 조회 - 성공")
    void get_user_post_success() throws Exception {

        PageImpl<PostListResponse> page = createPage(List.of(
                createPostResponse(1L, "title", "content1", "test", "profile.png", "0000-01-01", 10L),
                createPostResponse(2L, "title1", "content1", "test1", "profile1.png", "0000-01-01", 1L)
        ));

        when(postService.getAllPostByUser(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 게시글 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].postId").value(1L))
                .andExpect(jsonPath("$.data.content[0].title").value("title"))
                .andExpect(jsonPath("$.data.content[1].postId").value(2L))
                .andExpect(jsonPath("$.data.content[1].title").value("title1"));

        verify(postService).getAllPostByUser(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("마이페이지 사용자 댓글 조회 - 성공")
    void get_user_comments_success() throws Exception {

        PageImpl<CommentResponse> page = createPage(List.of(
                createCommentResponse(1L, 1L, "test comment", "0000-01-01", "1111-01-01"),
                createCommentResponse(2L, 1L, "test comment2", "0000-01-02", "1111-01-02")
        ));

        when(commentService.getCommentByUser(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 댓글 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].commentId").value(1L))
                .andExpect(jsonPath("$.data.content[0].content").value("test comment"))
                .andExpect(jsonPath("$.data.content[1].commentId").value(2L))
                .andExpect(jsonPath("$.data.content[1].content").value("test comment2"));

        verify(commentService).getCommentByUser(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("마이페이지 사용자 정보 수정 - 성공")
    void update_user_profile_success() throws Exception {

        UserUpdateDto dto = UserUpdateDto.builder()
                .nickname("newNick")
                .profileImage("newProfile.png")
                .build();

        UserDetailResponse response = createUserDetailResponse();

        when(userService.updateUser(any(UserUpdateDto.class), any()))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 정보 업데이트 성공"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.profileImage").value("profile.png"));

        verify(userService).updateUser(any(UserUpdateDto.class), any());
    }

    @Test
    @DisplayName("마이페이지 사용자 정보 수정 실패 - 닉네임 공백")
    void update_user_profile_fail_blank_nickname() throws Exception {

        UserUpdateDto dto = UserUpdateDto.builder()
                .nickname("")
                .profileImage("profile.png")
                .build();

        mockMvc.perform(patch(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임을 입력해주세요"));

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    @DisplayName("마이페이지 비밀번호 변경 - 성공")
    void change_password_success() throws Exception {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .build();

        mockMvc.perform(patch(BASE_URL + "/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).changePassword(any(ChangePasswordDto.class), any());
    }

    @Test
    @DisplayName("마이페이지 비밀번호 변경 - 실패(존재하지 않는 사용자)")
    void change_password_fail_no_user() throws Exception {

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .build();

        doThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND))
                .when(userService).changePassword(any(ChangePasswordDto.class), any());

        mockMvc.perform(patch(BASE_URL + "/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."));

        verify(userService).changePassword(any(ChangePasswordDto.class), any());
    }

    @Test
    @DisplayName("마이페이지 비밀번호 변경 - 실패(비밀번호 없음)")
    void change_password_fail_no_password() throws Exception {

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .passwordCheck("ABCD1234!")
                .build();

        mockMvc.perform(patch("/users/me/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호를 입력해주세요"));

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("마이페이지 비밀번호 변경 - 실패(비밀번호 확인 없음)")
    void change_password_fail_no_password_check() throws Exception {

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("1234")
                .passwordCheck("1234")
                .build();

        mockMvc.perform(patch("/users/me/pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상, 20자 이하이며 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."));

        verify(userService, never()).changePassword(any(), any());
    }


    @Test
    @DisplayName("마이페이지 회원 탈퇴 - 성공")
    void delete_user_success() throws Exception {
        mockMvc.perform(delete(BASE_URL))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).delete((User) any());
    }

    private UserDetailResponse createUserDetailResponse() {
        return UserDetailResponse.builder()
                .email("test@test.com")
                .nickname("tester")
                .createdDate("2024-01-01")
                .profileImage("profile.png")
                .build();
    }

    private PostListResponse createPostResponse(Long id, String title, String content, String writer,
                                                String profileImage, String createdDate, Long viewCount) {
        return PostListResponse.builder()
                .postId(id)
                .title(title)
                .content(content)
                .writer(writer)
                .profileImage(profileImage)
                .createdDate(createdDate)
                .viewCount(viewCount)
                .build();
    }

    private CommentResponse createCommentResponse(Long commentId, Long postId, String content,
                                                  String createdDate, String modifiedDate) {
        return CommentResponse.builder()
                .commentId(commentId)
                .postId(postId)
                .content(content)
                .writer("tester")
                .writerEmail("test@test.com")
                .createdDate(createdDate)
                .modifiedDate(modifiedDate)
                .build();
    }

    private <T> PageImpl<T> createPage(List<T> content) {
        return new PageImpl<>(content, PageRequest.of(0, content.size()), content.size());
    }
}
