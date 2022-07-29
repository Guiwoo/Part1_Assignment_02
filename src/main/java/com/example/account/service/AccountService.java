package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRespository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;

import static com.example.account.type.AccountStatus.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRespository accountRespository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자 있는지 확인
     * 계좌 번호 생성 하고
     * 계좌 저장 하고 정보 리턴
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initalBalance){
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(
                        () -> new AccountException(ErrorCode.USER_NOT_FOUND)
                );
        //Validating Accounts total
        validateCreateAccount(accountUser);

        String newAccountNumber = accountRespository.findFirstByOrderByIdDesc()
                .map(acc -> (Integer.parseInt(acc.getAccountNumber()))+1+ "")
                .orElse("1000000000");

        Account account = accountRespository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initalBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        );
        return AccountDto.fromEntity(account);
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if(accountRespository.countByAccountUser(accountUser) >= 10){
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id){
        return accountRespository.findById(id).get();
    }
}
