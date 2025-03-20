package com.hsbc.calculation.controller;

import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;
import com.hsbc.calculation.repository.TransactionRepository;
import com.hsbc.calculation.repository.UserAccountRepository;
import com.hsbc.calculation.service.RandomDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模拟生成数据
 */
@RestController
@RequestMapping("/mock")
public class MockGeneratorController {
    @Autowired
    private RandomDataService randomDataService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    @GetMapping("/generate-data")
    public String generateData(@RequestParam(defaultValue = "10") int numberOfAccounts,
                               @RequestParam(defaultValue = "5") int transactionsPerAccount) {
        List<UserAccountDO> accounts = randomDataService.generateRandomAccounts(numberOfAccounts);
        userAccountRepository.saveAll(accounts);

        List<TransactionDO> transactions = randomDataService.generateRandomTransactions(accounts, transactionsPerAccount);
        transactionRepository.saveAll(transactions);

        return "Generated " + numberOfAccounts + " accounts and " + (numberOfAccounts * transactionsPerAccount) + " transactions.";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
