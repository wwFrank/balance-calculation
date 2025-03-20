package com.hsbc.calculation.utils;

import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConvertUtil {
    public static final String TEST_ACCOUNT_NUMBER = "123456";
    public static final String TEST_TARGET_ACCOUNT = "678910";
    public static final int INI_BALANCE = 1000;
    public static final int CHANGE_BALANCE = 1024;
    public static UserAccountDO buildUserAccountDO(String accountNumber, int balance) {
        UserAccountDO userAccountDO = new UserAccountDO();
        userAccountDO.setAccountNumber(accountNumber);
        userAccountDO.setBalance(new BigDecimal(balance));
        return userAccountDO;
    }

    public static TransactionDO buildTransactionDO(String sourceAccountNumber, String targetAccountNumber, int amount) {
        TransactionDO transaction = new TransactionDO();
        transaction.setSourceAccountNumber(sourceAccountNumber);
        transaction.setTargetAccountNumber(targetAccountNumber);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setStatus(TransactionConstants.TRANSACTION_STATUS_SUCCESS);
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }
}
