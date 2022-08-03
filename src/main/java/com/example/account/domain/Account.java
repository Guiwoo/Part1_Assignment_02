package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.AccountType;
import com.example.account.type.ErrorCode;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account extends BaseEntity {

    @ManyToOne
    private AccountUser accountUser;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_number_id")
    private AccountNumber accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;


    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisterdAt;

    public void useBalance(Long amount){
        if(amount > this.balance){
            throw new AccountException((ErrorCode.AMOUNT_EXCEED_BALANCE));
        }
        balance -= amount;
    }

    public void cancelBalance(Long amount){
        if(amount < 0){
            throw new AccountException((ErrorCode.INVALID_REQUEST));
        }
        balance += amount;
    }
}
