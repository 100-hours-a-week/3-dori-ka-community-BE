package com.example.community.controller;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.response.user.SignUpResponse;

import com.example.community.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {


    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("test")
                .profileImage("profile.png")
                .build();

        SignUpResponse response = new SignUpResponse("test@test.com");

        when(userService.signUp(any(UserSignUpDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("test@test.com"));

        verify(userService, times(1)).signUp(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_emailDuplicate() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("test")
                .profileImage("profile.png")
                .build();

        when(userService.signUp(any(UserSignUpDto.class)))
                .thenThrow(new DuplicatedException(ErrorMessage.EMAIL_DUPLICATED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorMessage.EMAIL_DUPLICATED.getMessage()));
    }


    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signUp_fail_nicknameDuplicate() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("test")
                .profileImage("profile.png")
                .build();

        when(userService.signUp(any(UserSignUpDto.class)))
                .thenThrow(new DuplicatedException(ErrorMessage.NICKNAME_DUPLICATED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorMessage.NICKNAME_DUPLICATED.getMessage()));
    }

}