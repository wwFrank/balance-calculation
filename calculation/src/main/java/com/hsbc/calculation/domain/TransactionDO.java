package com.hsbc.calculation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionDO {
    /**
     * 交易id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;
    /**
     * 交易状态
     * SUCCESS: 成功
     * FAILED: 失败
     */
    @Getter
    @Setter
    private String status;
    /**
     * 源账户账号
     */
    @Getter
    @Setter
    private String sourceAccountNumber;
    /**
     * 目标账户账号
     */
    @Getter
    @Setter
    private String targetAccountNumber;
    /**
     * 交易金额
     */
    @Getter
    @Setter
    private BigDecimal amount;
    /**
     * 交易时间
     */
    @Getter
    @Setter
    private LocalDateTime timestamp;
}

