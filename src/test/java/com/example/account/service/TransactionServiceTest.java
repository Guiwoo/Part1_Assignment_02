package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;

import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.TransactionResultType.*;
import static com.example.account.type.TransactionType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("계좌거래_성공")
    void successUseBalance(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(a));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(a)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapShot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService
                .useBalance(1L, "1000000000", 3000L);
        //then
        verify(transactionRepository,times(1)).save(captor.capture());
        assertEquals(3000L,captor.getValue().getAmount());
        assertEquals(7000L,captor.getValue().getBalanceSnapShot());
        assertEquals(9000L,transactionDto.getBalanceSnapShot());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(USE,transactionDto.getTransactionType());
        assertEquals(1000L,transactionDto.getAmount());
    }

    @Test
    @DisplayName("계좌거래_실패/유저가 없다면")
    void transactionUserNotFound(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.useBalance(1L, "1234567890",100L)
        );
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래_실패/계좌가 없다면")
    void deleteAccountAccountNotFound(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());


        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.useBalance(1L, "1234567890",300L)
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래_실패/소유자가 다르다면")
    void deleteAccountUserUnmatched(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        AccountUser pobi2 = AccountUser.builder()
                .id(13L)
                .name("Bipo").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi2)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.useBalance(1L, "1234567890",100L)
        );
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCHED,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래_실패/이미 해지된 경우")
    void deleteAccountAlreadyUnregistered(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(100L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.useBalance(1L, "1234567890",100L)
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래_실패/거래 금액이 큰경우")
    void amountExceedBalance(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(100L)
                .accountNumber("1000000000").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(a));
        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.useBalance(1L, "1234567890",400L)
        );
        //then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE,exception.getErrorCode());
        verify(transactionRepository,times(0)).save(any());
    }

    @Test
    @DisplayName("계좌거래_실패/저장")
    void failedUseBalance(){
        //given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(a));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(a)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapShot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);
        //when
        transactionService.saveFailedUseTransaction("1000000000", 3000L);
        //then
        verify(transactionRepository,
                times(1)).save(captor.capture());
        assertEquals(3000L,captor.getValue().getAmount());
        assertEquals(10000L,captor.getValue().getBalanceSnapShot());
        assertEquals(F,captor.getValue().getTransactionResultType());

    }
}