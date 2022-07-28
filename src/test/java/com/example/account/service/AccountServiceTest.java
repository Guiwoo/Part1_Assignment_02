package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.repository.AccountRespository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRespository accountRespository;
    @InjectMocks
    private AccountService accountService;
    @Test
    @DisplayName("계좌조회 성공")
    void testxxx(){
        //given
        given(accountRespository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65874").build()));
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        //when
        Account account = accountService.getAccount(3453L);
        //then
        verify(accountRespository,times(1))
                .findById(captor.capture());
        verify(accountRespository,times(0)).save(any());

        assertEquals(3453L,captor.getValue());
        assertNotEquals(531243L,captor.getValue());
        assertEquals("65874",account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED,account.getAccountStatus());
    }

    @Test
    @DisplayName("계좌조회 실패 - 음수로 조회")
    void testFailToSearchAccount(){
        //given
        //when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.getAccount(-10L));
        //then
        assertEquals("Minus",exception.getMessage());
    }

    @Test
    @DisplayName("✅이름 검사")
    void testGetAcc(){
        //given
        accountService.createAccount();
        //when
        Account acc = accountService.getAccount(1L);
        //then
        assertEquals("1512945",acc.getAccountNumber());
        assertEquals(AccountStatus.IN_USE,acc.getAccountStatus());
    }
    @Test
    void testGetAcc2(){
        //given
        accountService.createAccount();
        //when
        Account acc = accountService.getAccount(2L);
        //then
        assertEquals("1512945",acc.getAccountNumber());
        assertEquals(AccountStatus.IN_USE,acc.getAccountStatus());
    }
}