package com.naeggeodo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@SpringBootTest
@Sql("classpath:h2/userController.sql")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build()
        ;

    }

    @Test
    void updateAddress() {
    }

    @Test
    void getAddress() {
    }


    //
    @Test
    void getMyPageDate() throws Exception {
        String userId = "user0";

        MvcResult mvcResult = mockMvc.perform(get("/user/{user_id}/mypage", userId))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(contentAsString);

        assertAll(
                () -> assertThat(jsonObject.get("participatingChatCount")).isEqualTo(1),
                () -> assertThat(jsonObject.get("myOrdersCount")).isEqualTo(1),
                () -> assertThat(jsonObject.get("nickname")).isEqualTo("도봉산-왕주먹")
        );
    }

    @Test
    void getQuickChat() {
    }

    @Test
    void updateQuickChat() {
    }

    @Test
    void getNickname() {
    }

    @Test
    void updateNickname() {
    }
}