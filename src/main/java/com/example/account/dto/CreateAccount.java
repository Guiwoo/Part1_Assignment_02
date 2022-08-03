package com.example.account.dto;

import com.example.account.domain.AccountNumber;
import com.example.account.type.AccountType;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateAccount {
    @Getter
    @Setter
    @AllArgsConstructor
    public static  class Request{
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Min(0)
        private Long initialBalance;

        @NotNull
        private AccountType accountType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static  class Response{
        private Long userId;
        private AccountNumber accountNumber;
        private LocalDateTime registeredAt;
        private AccountType accountType;

        public static Response from(AccountDto accountDto){
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountType(accountDto.getAccountType())
                    .accountNumber(accountDto.getAccountNumber())
                    .registeredAt(accountDto.getRegisteredAt())
                    .build();
        }
    }
}
