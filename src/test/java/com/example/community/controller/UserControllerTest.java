package com.example.community.controller;

import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;

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
    void register_success() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("tester")
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
}