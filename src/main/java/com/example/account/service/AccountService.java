package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.repository.AccountRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRespository accountRespository;

    @Transactional
    public void createAccount(){
        Account account = Account.builder()
                .accountNumber("1234")
                .accountStatus(AccountStatus.IN_USE).build();
        accountRespository.save(account);
    }

    @Transactional
    public Account getAccount(Long id){
        return accountRespository.findById(id).get();
    }
}
