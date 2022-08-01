package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류 입니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다"),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지 되었습니다."),
    ACCOUNT_TRANSACTION_LOCK("계좌 는 사용중 입니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 계좌 잔고 보다 큽니다."),
    BALANCE_NOT_EMPTY("잔고 가 있어, 계좌해지 불가능"),
    CANCEL_MUST_FULLY("부분 취소는 붕가능 합니다."),
    INVALID_REQUEST("잘못된 요청 입니다."),
    MAX_ACCOUNT_PER_USER_10("사용자 최대 계좌 10 개 입니다."),
    USER_ACCOUNT_UNMATCHED("사용자 와 계좌 소유주 가 다릅니다."),
    TRANSACTION_NOT_FOUND("해당 거래가 없습니다"),
    TOO_OLD_ORDER_TO_CANCEL("1년이 지난 거래는 취소가 불가능 합니다."),
    TRANSACTION_ACCOUNT_UNMATCHED("이 거래 는 해당 계좡에서 발생한 거래가 아닙니다."),
    USER_NOT_FOUND("사용자가 없습니다.");
    private final String description;
}
