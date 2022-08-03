package com.example.account.repository;

import com.example.account.domain.AccountNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountNumberRepository extends JpaRepository<AccountNumber,Long> {
    boolean existsAccountNumbersByAccountNumber(String accountNumber);
}
