package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Integer countByAccountUser(AccountUser accountUser);
    Optional<Account> findByAccountNumber(String AccountNumber);
    boolean existsAccountByAccountNumber(String AccountNumber);
    List<Account> findByAccountUser(AccountUser account);
}
