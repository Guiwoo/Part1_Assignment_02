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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static com.example.account.type.AccountStatus.*;
import static com.example.account.type.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRespository;
    private final AccountUserRepository accountUserRepository;
    private final AccountNumberRepository accountNumberRepository;

    /**
     * 사용자 있는지 확인
     * 계좌 번호 생성 하고
     * 계좌 저장 하고 정보 리턴
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance,AccountType accType){
        AccountUser accountUser = getAccountUser(userId);
        validateCreateAccount(accountUser);
        //account number setting
        String newAcc;
        while(true){
            System.out.println("Generating Account...");
            newAcc = generateAccountNumber(accType);
            boolean get = accountNumberRepository.existsAccountNumbersByAccountNumber(newAcc);
            if(!get){
                break;
            }
        }

        Account account = accountRespository.save(
                Account.builder()
                        .accountType(accType)
                        .accountNumber(AccountNumber.builder()
                                .accountNumber(newAcc)
                                .build())
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        );

        return AccountDto.fromEntity(account);
    }
    private String generateAccountNumber(AccountType accountType){
        String genAcc= "";
        switch (accountType){
            case CHECKING:
                genAcc+="1000";
                break;
            case SAVING:
                genAcc+="2000";
                break;
            case MONEY_MARKET:
                genAcc+="3000";
                break;
            case CERTIFICATE_OF_DEPOSIT:
                genAcc+="4000";
                break;
        }
        Random r = new Random();
        for(int i=0;i<6;i++){
            genAcc+=r.nextInt(10);
        }
        System.out.println("✅"+ genAcc);
        return genAcc.toString();
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(
                        () -> new AccountException(USER_NOT_FOUND)
                );
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

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);
        Account account = accountRespository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND)
                );
        validateDeleteAccount(accountUser,account);

        account.setAccountStatus(UNREGISTERED);
        account.setUnRegisterdAt(LocalDateTime.now());
        // 테스트를 위한 추가코드
        accountRespository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        if(!Objects.equals(accountUser.getId(), account.getAccountUser().getId())){
            throw new AccountException(ErrorCode.USER_ACCOUNT_UNMATCHED);
        }
        if(account.getAccountStatus() == AccountStatus.UNREGISTERED){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance() > 0L){
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }

    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);
        List<Account> accounts = accountRespository.findByAccountUser(accountUser);
        return accounts.stream()
                .map(AccountDto::fromEntity).collect(Collectors.toList());
    }
}
