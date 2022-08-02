package com.example.account.controller;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.exception.AccountException;
import com.example.account.service.AccountService;
import com.example.account.type.AccountType;
import com.example.account.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @MockBean
    private AccountService accountService;
    //Injection 이 필요가 없음

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("계좌생성")
    void sucessCreateAccount() throws Exception {
        //given
        given(accountService.createAccount(anyLong(),anyLong(),any()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountType(AccountType.CHECKING)
                        .accountNumber("123456789")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());

        //when
        //then
        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateAccount.Request(1L,100L,AccountType.CHECKING)
                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("123456789"))
                .andDo(print());
    }
    @Test
    @DisplayName("계좌삭제")
    void deleteCreateAccount() throws Exception {
        //given
        given(accountService.deleteAccount(anyLong(),anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("123456789")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());

        //when
        //then
        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccount.Request(1L,"0123456789")
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("123456789"))
                .andDo(print());
    }

    @Test
    @DisplayName("계좌찾기")
    void successGetAccountByUserId() throws Exception {
        //given
        List<AccountDto> accountDtos = Arrays.asList(
                AccountDto.builder()
                        .accountNumber("1234567890")
                        .balance(1000L)
                        .build(),
                AccountDto.builder()
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                AccountDto.builder()
                        .accountNumber("2222222222")
                        .balance(1000L)
                        .build()
        );
        given(accountService.getAccountByUserId(anyLong()))
                .willReturn(accountDtos);

        //when
        //then
        mockMvc.perform(get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$[0].accountNumber")
                        .value("1234567890"))
                .andExpect(jsonPath("$[0].balance")
                        .value(1000L))
                .andExpect(jsonPath("$[1].accountNumber")
                        .value("1111111111"))
                .andExpect(jsonPath("$[1].balance")
                        .value(1000L));

    }

    @Test
    @DisplayName("계좌찾기")
    void failGetAccountByUserId() throws Exception {

        given(accountService.getAccountByUserId(anyLong()))
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        //when
        //then
        mockMvc.perform(get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$.errorCode")
                        .value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("계좌가 없습니다"));

    }
}