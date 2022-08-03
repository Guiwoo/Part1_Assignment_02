package com.example.account.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class AccountNumber extends BaseEntity{
    // 단순 조회용 테이블
    private String accountNumber ;

//    @OneToOne(mappedBy ="accountNumber")
//    private Account account;
}
