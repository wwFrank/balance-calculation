package com.hsbc.calculation.repository;

import com.hsbc.calculation.domain.TransactionDO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用JPA实现交易事务
 */
public interface TransactionRepository extends JpaRepository<TransactionDO, Long> {
}

