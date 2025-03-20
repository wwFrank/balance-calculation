package com.hsbc.calculation.service;
import com.github.javafaker.Faker;
import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class RandomDataService {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    public UserAccountDO generateRandomAccount() {
        UserAccountDO account = new UserAccountDO();
        account.setAccountNumber(faker.number().digits(10));
        BigDecimal bigDecimal = new BigDecimal(random.nextDouble() * 10000).setScale(2, BigDecimal.ROUND_HALF_UP);
        account.setBalance(bigDecimal);
        return account;
    }

    public TransactionDO generateRandomTransaction(UserAccountDO account, String targetAccountNumber) {
        TransactionDO transaction = new TransactionDO();
        transaction.setId((long)(random.nextDouble() * 10000) + 10000);
        BigDecimal bigDecimal = new BigDecimal(random.nextDouble() * 5000 - 2500).setScale(2, BigDecimal.ROUND_HALF_UP);
        transaction.setAmount(bigDecimal);
        transaction.setSourceAccountNumber(account.getAccountNumber());
        transaction.setTargetAccountNumber(targetAccountNumber);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionConstants.TRANSACTION_STATUS_SUCCESS);
        return transaction;
    }

    public List<UserAccountDO> generateRandomAccounts(int count) {
        List<UserAccountDO> accounts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            accounts.add(generateRandomAccount());
        }
        return accounts;
    }

    public List<TransactionDO> generateRandomTransactions(List<UserAccountDO> accounts, int transactionsPerAccount) {
        List<TransactionDO> transactions = new ArrayList<>();
        for(int j = 0; j < accounts.size()-1; j++) {
            for (int i = 0; i < transactionsPerAccount; i++) {
                transactions.add(generateRandomTransaction(accounts.get(j), accounts.get(j+1).getAccountNumber() ));
            }
        }
        return transactions;
    }
}