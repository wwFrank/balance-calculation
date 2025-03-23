package com.hsbc.calculation.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "user_account")
public class UserAccountDO implements Serializable {
    @Id
    @Getter
    @Setter
    private String accountNumber;
    @Getter
    @Setter
    private BigDecimal balance;
}