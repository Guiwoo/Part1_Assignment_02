package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import static com.example.account.type.TransactionResultType.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("계좌거래_성공")
    void successUseBalance() throws Exception {
        //given
        given(transactionService.useBalance(anyLong(),anyString(),anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactedAt(LocalDateTime.now())
                        .amount(12345L)
                        .transactionId("transactionId")
                        .transactionResultType(S)
                        .build());
        //when
        //then
        mockMvc.perform(post("/transaction/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UseBalance.Request(
                                1L,"1000000000",1000L
                        )
                ))
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accountNumber").value("1000000000")
                ).andExpect(
                        jsonPath("$.transactionId").value("transactionId")
                ).andExpect(
                        jsonPath("$.amount").value(12345L)
                );
    }

    @Test
    @DisplayName("계좌거래취소_성공")
    void successCancelBalance() throws Exception {
        //given
        given(transactionService.cancleBalance(anyString(),anyString(),anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactedAt(LocalDateTime.now())
                        .amount(54321L)
                        .transactionId("transactionIdForCancel")
                        .transactionResultType(S)
                        .build());
        //when
        //then
        mockMvc.perform(post("/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelBalance.Request(
                                        "transactionId","1000000000",1000L
                                )
                        ))
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accountNumber")
                                .value("1000000000")
                ).andExpect(
                        jsonPath("$.transactionId")
                                .value("transactionIdForCancel")
                ).andExpect(
                        jsonPath("$.amount")
                                .value(54321L)
                );
    }
    @Test
    @DisplayName("거래내역 확인")
    void successGetQuery() throws Exception {
        //given

        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactionType(TransactionType.USE)
                        .transactedAt(LocalDateTime.now())
                        .amount(54321L)
                        .transactionId("trGet")
                        .transactionResultType(S)
                        .build());

        //when
        //then
        mockMvc.perform(get("/transaction/123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accountNumber")
                                .value("1000000000")
                ).andExpect(
                        jsonPath("$.transactionType")
                                .value("USE")
                ).andExpect(
                        jsonPath("$.transactionResult")
                                .value("S")
                ).andExpect(
                        jsonPath("$.transactionId")
                                .value("trGet")
                ).andExpect(
                        jsonPath("$.amount")
                                .value(54321L)
                );
    }
}