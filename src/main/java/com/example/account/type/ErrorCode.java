package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ACCOUNT_NOT_FOUND("계좌가 없습니다"),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지 되었습니다."),
    BALANCE_NOT_EMPTY("잔고 가 있어, 계좌해지 불가능"),
    MAX_ACCOUNT_PER_USER_10("사용자 최대 계좌 10 개 입니다."),
    USER_ACCOUNT_UNMATCHED("사용자 와 계좌 소유주 가 다릅니다."),
    USER_NOT_FOUND("사용자가 없습니다.");
    private final String description;
}
