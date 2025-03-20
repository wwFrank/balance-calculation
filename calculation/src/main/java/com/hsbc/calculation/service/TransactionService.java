package com.hsbc.calculation.service;

import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;
import com.hsbc.calculation.repository.TransactionRepository;
import com.hsbc.calculation.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * 交易余额服务核心业务逻辑
 * 采用重试机制、缓存和数据库锁解决并发问题
 */
@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserRedisService userRedisService;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * 处理交易接口
     * 触发重试机制，捕捉RuntimeException重试
     * Transactional 确保事务在方法执行期间保持一致性，即使出现异常
     * @param transaction
     * @return
     */
    @Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(rollbackFor = {SQLException.class, RuntimeException.class}, noRollbackFor = {IllegalArgumentException.class})
    public TransactionDO processTransaction(TransactionDO transaction) {
        // 从缓存中获取余额
        String sourceNo = transaction.getSourceAccountNumber();
        String targetNo = transaction.getTargetAccountNumber();
        UserAccountDO sourceAccountDO = userRedisService.getUserAccountFromCache(sourceNo);
        UserAccountDO targetAccountDO = userRedisService.getUserAccountFromCache(targetNo);

        // 如果缓存中没有，从数据库加载用户信息
        if (sourceAccountDO == null) {
            sourceAccountDO = userAccountRepository.findById(transaction.getSourceAccountNumber()).orElseThrow(() -> new IllegalArgumentException("Source account=" + sourceNo + " not found"));
        }
        if (targetAccountDO == null) {
            targetAccountDO = userAccountRepository.findById(transaction.getTargetAccountNumber()).orElseThrow(() -> new IllegalArgumentException("Target account=" + targetNo+" not found"));
        }

        BigDecimal sourceBalance = sourceAccountDO.getBalance();
        BigDecimal targetBalance = targetAccountDO.getBalance();

        // 验证余额
        if (sourceBalance.compareTo(transaction.getAmount()) < 0) {
            logger.warn("processTransaction transaction amount insufficient error:sourceBalance={},amount={}",sourceBalance, transaction.getAmount());
        }

        // 更新余额
        sourceBalance = sourceBalance.subtract(transaction.getAmount());
        targetBalance = targetBalance.add(transaction.getAmount());

        //更新账户余额
        sourceAccountDO.setBalance(sourceBalance);
        targetAccountDO.setBalance(targetBalance);
        UserAccountDO saveSourceAccountDO = userAccountRepository.save(sourceAccountDO);
        if(saveSourceAccountDO == null) {
            logger.warn("processTransaction save source account failed:sourceAccount={}, targetAccount={}, transactionId={}",sourceNo, targetNo, transaction.getId());
            throw new RuntimeException("Save source account failed");
        }
        UserAccountDO saveTargetAccountDO = userAccountRepository.save(sourceAccountDO);
        if(saveTargetAccountDO == null) {
            logger.warn("processTransaction save target account failed:sourceAccount={}, targetAccount={}, transactionId={}",sourceNo, targetNo, transaction.getId());
            throw new RuntimeException("Save target account failed");
        }
        // 更新缓存
        userRedisService.updateUserAccount(transaction.getSourceAccountNumber(), sourceAccountDO);
        userRedisService.updateUserAccount(transaction.getTargetAccountNumber(), targetAccountDO);

        // 保存交易记录
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionConstants.TRANSACTION_STATUS_SUCCESS);

        TransactionDO save = transactionRepository.save(transaction);
        if(save == null) {
            logger.warn("processTransaction save transaction failed:sourceAccount={}, targetAccount={}, amount={}",sourceNo, targetNo, transaction.getAmount());
            throw new RuntimeException("Save transaction failed");
        }
        return save;
    }
}
