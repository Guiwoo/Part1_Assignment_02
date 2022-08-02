package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    CHECKING("1000"),
    SAVING("2000"),
    MONEY_MARKET("3000"),
    CERTIFICATE_OF_DEPOSIT("4000");
    private final String description;
}