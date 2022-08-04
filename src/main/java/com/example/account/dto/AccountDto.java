package com.example.account.dto;

import com.example.account.domain.Account;
import com.example.account.type.AccountType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
 private Long userId;
 private String accountNumber;
 private Long balance;
 private AccountType accountType;

 private LocalDateTime registeredAt;
 private LocalDateTime unRegisteredAt;

 public static AccountDto fromEntity(Account account){
   return AccountDto.builder()
           .userId(account.getAccountUser().getId())
           .accountType(account.getAccountType())
           .balance(account.getBalance())
           .accountNumber(account.getAccountNumber())
           .registeredAt(account.getCreatedAt())
           .unRegisteredAt(account.getUnRegisterdAt())
           .build();
 }
}
