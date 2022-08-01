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
                .name("pobi").build();
        pobi.setId(12L);
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
                .name("pobi").build();
        pobi.setId(12L);
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
                .name("pobi").build();
        pobi.setId(12L);
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
                .name("Pobi").build();
        pobi.setId(12L);
        AccountUser pobi2 = AccountUser.builder()
                .name("Bipo").build();
        pobi2.setId(13L);
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
                .name("Pobi").build();
        pobi.setId(12L);
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
                .name("pobi").build();
        pobi.setId(12L);
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
                .name("pobi").build();
        pobi.setId(12L);
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

    @Test
    @DisplayName("계좌거래취_성공")
    void successCancelBalance(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();
        Transaction transaction = Transaction.builder()
                .account(a)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(3000L)
                .balanceSnapShot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(a));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(a)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("transactionIdForCancel")
                        .transactedAt(LocalDateTime.now())
                        .amount(3000L)
                        .balanceSnapShot(10000L)
                        .build());
        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService
                .cancleBalance("transactionId",
                        "1000000000",
                        3000L);
        //then
        verify(transactionRepository,times(1)).save(captor.capture());
        assertEquals(3000L,captor.getValue().getAmount());
        assertEquals(13000L,captor.getValue().getBalanceSnapShot());
        assertEquals(10000L,transactionDto.getBalanceSnapShot());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(CANCEL,transactionDto.getTransactionType());
        assertEquals(3000L,transactionDto.getAmount());
    }

    @Test
    @DisplayName("계좌거래취소_실패/계좌가 없다면")
    void cancelTransaction_AccountNotFound(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        Transaction transaction = Transaction.builder().build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());


        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.cancleBalance(
                        "transactionId",
                        "1234567890",
                        300L)
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래취소_실패/거래가 없다면")
    void cancelTransaction_TransactionNotFound(){

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.cancleBalance(
                        "transactionId",
                        "1234567890",
                        300L)
        );
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래취소_실패/거래 계좌 매칭 실패")
    void cancelTransaction_TransactionAccountUnMatched(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();
        a.setId(1L);
        Account b = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000001").build();
        b.setId(2L);
        Transaction transaction = Transaction.builder()
                .account(a)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(3000L)
                .balanceSnapShot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(b));


        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.cancleBalance(
                        "transactionId",
                        "1234567890",
                        3000L)
        );
        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UNMATCHED,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래취소_실패/거래금액 과 취소금액이 다름")
    void cancelTransaction_TransactionAmountUnMatched(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);

        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();
        a.setId(1L);
        Transaction transaction = Transaction.builder()
                .account(a)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(3001L)
                .balanceSnapShot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(a));


        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.cancleBalance(
                        "transactionId",
                        "1234567890",
                        3000L)
        );
        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌거래취소_실패/거래취소 기한 초과")
    void cancelTransaction_TransactionCancelExpired(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();
        a.setId(1L);
        Transaction transaction = Transaction.builder()
                .account(a)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1)
                        .minusDays(1))
                .amount(3000L)
                .balanceSnapShot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(a));


        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.cancleBalance(
                        "transactionId",
                        "1234567890",
                        3000L)
        );
        //then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL,exception.getErrorCode());
    }

    @Test
    @DisplayName("거래내역조회_성공")
    void successQueryTransaction(){
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        Account a = Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000").build();
        a.setId(1L);
        Transaction transaction = Transaction.builder()
                .account(a)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1)
                        .minusDays(1))
                .amount(3000L)
                .balanceSnapShot(9000L)
                .build();
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");
        //then
        assertEquals(USE,transactionDto.getTransactionType());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(3000L,transactionDto.getAmount());
        assertEquals("transactionId",transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("거래조회_실패/거래가 없다면")
    void transactionSearch_TransactionNotFound(){

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->transactionService.queryTransaction(anyString())
        );
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND,exception.getErrorCode());
    }
}