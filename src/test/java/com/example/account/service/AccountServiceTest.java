package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountNumber;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountNumberRepository;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.AccountType;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRespository;
    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private AccountNumberRepository accountNumberRepository;

    @InjectMocks
    private AccountService accountService;

//    @Test 318 ms
//    @DisplayName("계좌생성_성공/동일 계좌가 있을시")
//    void createAccountSuccess1(){
//        //given
//        AccountUser pobi = AccountUser.builder()
//                .name("pobi").build();
//        pobi.setId(12L);
//        given(accountUserRepository.findById(anyLong()))
//                .willReturn(Optional.of(pobi));
//        //중복 터트려줄 모킹
//        given(accountRespository.countAccountByAccountNumber(anyString()))
//                .willReturn(1) //중복
//                .willReturn(0); //중복아님
//
//        given(accountRespository.save(any()))
//                .willReturn(Account.builder()
//                        .accountUser(pobi)
//                        .accountType(AccountType.CHECKING)
//                        .accountNumber("1000000013").build());
//
//        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//
//        //when
//        AccountDto accountDto = accountService.createAccount(
//                1L, 100L, AccountType.CHECKING
//        );
//        //then
//        // 2번 실행되기 때문에 2번이 실행된지 확인여부
//        verify(accountRespository,times(2))
//                .countAccountByAccountNumber(any());
//        assertEquals(12L,accountDto.getUserId());
//    }

    //301ms
    @Test
    @DisplayName("계좌생성_성공/동일 계좌가 있을시")
    void createAccountSuccess(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        //중복 터트려줄 모킹
        given(accountNumberRepository.existsAccountNumbersByAccountNumber(anyString()))
                .willReturn(true) //중복
                .willReturn(true)
                .willReturn(true)
                .willReturn(false); //중복아님

        given(accountNumberRepository.save(any()))
                .willReturn(AccountNumber.builder().accountNumber("1000000013").build());

        given(accountRespository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(pobi)
                        .accountType(AccountType.CHECKING)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(
                1L, 100L, AccountType.CHECKING
        );
        //then
        // 2번 실행되기 때문에 2번이 실행된지 확인여부
        verify(accountNumberRepository,times(4))
                .existsAccountNumbersByAccountNumber(any());
        assertEquals(12L,accountDto.getUserId());
    }

//    @Test 랜덤 넘버생성 이기 때문에 테스트코드 스킵
//    @DisplayName("계좌생성_성공/첫번째 계좌인 경우")
//    void createFirstAccount(){
//        //given
//        AccountUser pobi = AccountUser.builder()
//                .name("pobi").build();
//        pobi.setId(15L);
//        given(accountUserRepository.findById(anyLong()))
//                .willReturn(Optional.of(pobi));
//        //계좌가 없는경우 테스트
//        given(accountRespository.findFirstByOrderByIdDesc())
//                .willReturn(Optional.empty());
//
//        given(accountRespository.save(any()))
//                .willReturn(Account.builder()
//                        .accountUser(pobi)
//                        .accountNumber("1000000013").build());
//        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//
//        //when
//        AccountDto accountDto = accountService.createAccount(
//                1L, 100L,AccountType.CHECKING
//        );
//        //then
//        verify(accountRespository,times(1))
//                .save(captor.capture());
//        assertEquals(15L,accountDto.getUserId());
//        assertEquals("1000000000",captor.getValue().getAccountNumber());
//    }

    @Test
    @DisplayName("계좌생성_실패/유저 가 없다면")
    void createAccountUserNotFound(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.createAccount(1L, 100L,AccountType.CHECKING)
        );
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌생성_실패/10개가 넘는다면")
    void createAccount_maxAccountIs10(){
        //given
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRespository.countByAccountUser(any()))
                .willReturn(10);
        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.createAccount(1L, 100L,AccountType.CHECKING)
        );
        //then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌삭제_성공")
    void deleteAccountSuccess(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRespository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(0L)
                        .accountNumber("1000000012").build()));


        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.deleteAccount(
                1L, "1000000012"
        );
        //then
        verify(accountRespository,times(1))
                .save(captor.capture());
        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000012",captor.getValue().getAccountNumber());

        assertEquals(AccountStatus.UNREGISTERED,captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("계좌해지_실패/유저가 없다면")
    void deleteAccountUserNotFound(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L, "1234567890")
        );
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌삭제_실패/계좌가 없다면")
    void deleteAccountAccountNotFound(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRespository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());


        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L, "1234567890")
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND,exception.getErrorCode());
    }
    @Test
    @DisplayName("계좌해지_실패/소유자가 다르다면")
    void deleteAccountUserUnmatched(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);
        AccountUser pobi2 = AccountUser.builder()
                .name("Bipo").build();
        pobi2.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRespository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi2)
                        .balance(0L)
                        .accountNumber("123456789").build()));

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L, "1234567890")
        );
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCHED,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌해지_실패/잔액이 남아있는경우")
    void deleteAccountLeftBalance(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRespository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(100L)
                        .accountNumber("123456789").build()));

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L, "1234567890")
        );
        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY,exception.getErrorCode());
    }
    @Test
    @DisplayName("계좌해지_실패/이미 해지된 경우")
    void deleteAccountAlreadyUnregistered(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRespository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                                .accountType(AccountType.CHECKING)
                        .balance(100L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("123456789").build()));

        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L, "1234567890")
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌찾기_성공")
    void successGetAccountByUserId(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRespository.findByAccountUser(any()))
                .willReturn(accounts);

        //when
        List<AccountDto> accountDtos = accountService.getAccountByUserId(
                1L
        );

        //then
        assertEquals(3,accountDtos.size());
        assertEquals("1111111111",accountDtos.get(0).getAccountNumber());
        assertEquals(1000L,accountDtos.get(0).getBalance());
    }

    @Test
    @DisplayName("계좌찾기_실패/아이디 존재하지 않을때")
    void failedToGetAccountByUserId(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception =  assertThrows(AccountException.class,
                ()->accountService.getAccountByUserId(1L)
        );
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());
    }

}