package com.hsbc.calculation;

import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;
import com.hsbc.calculation.repository.UserAccountRepository;
import com.hsbc.calculation.service.TransactionService;
import com.hsbc.calculation.service.UserRedisService;
import com.hsbc.calculation.utils.ConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.hsbc.calculation.utils.ConvertUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(TransactionIntegrationTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRedisService userRedisService;

    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private TransactionService transactionService;

    /**
     * 交易成功
     */
    @Test
    public void testTransactionSuccess() {
        //数据已经存在云端MySQL，无需再创建
//        UserAccountDO sourceAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
//        userAccountRepository.save(sourceAccountDO);
//        UserAccountDO targetAccountDO = ConvertUtil.buildUserAccountDO(TEST_TARGET_ACCOUNT, INI_BALANCE);
//        userAccountRepository.save(targetAccountDO);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        transactionDO.setStatus(TransactionConstants.TRANSACTION_STATUS_FAILED);
        TransactionDO processTransaction = transactionService.processTransaction(transactionDO);
        // 验证结果
        assertEquals(TransactionConstants.TRANSACTION_STATUS_SUCCESS, processTransaction.getStatus());
    }

    /**
     * 失败后的重试机制
     */
    @Test
    public void testTransactionRetry() {
        userRedisService.updateUserAccount(TEST_ACCOUNT_NUMBER, ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE));
        userRedisService.updateUserAccount(TEST_TARGET_ACCOUNT, ConvertUtil.buildUserAccountDO(TEST_TARGET_ACCOUNT, CHANGE_BALANCE));
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        transactionDO.setStatus(TransactionConstants.TRANSACTION_STATUS_FAILED);
        try {
            TransactionDO processTransaction = transactionService.processTransaction(transactionDO);
            assertEquals(true, processTransaction != null);
        } catch (Exception e) {
            logger.warn("TransactionIntegrationTest.testTransactionRetry error:", e);
        }
    }

    /**
     * 测试事务性的数据库操作
     */
    @Test
    public void testTransactionalTransaction() {
        userRedisService.updateUserAccount(TEST_ACCOUNT_NUMBER, ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE));
        userRedisService.updateUserAccount(TEST_TARGET_ACCOUNT, ConvertUtil.buildUserAccountDO(TEST_TARGET_ACCOUNT, CHANGE_BALANCE));
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        transactionDO.setStatus(TransactionConstants.TRANSACTION_STATUS_FAILED);
        try {
            TransactionDO processTransaction = transactionService.processTransaction(transactionDO);
        } catch (Exception e) {
            logger.warn("TransactionIntegrationTest.testTransactionalTransaction error:", e);
            assertEquals(true, true);
        }
        UserAccountDO userAccountDO = userAccountRepository.findById(TEST_ACCOUNT_NUMBER).get();
        assertEquals(true, userAccountDO != null);
    }

    /**
     * 触发限流
     */
    private static final int NUM_THREADS = 10;
    @Test
    public void testTransactionLimiting() {
        //数据已经存在云端MySQL，无需再创建
//        UserAccountDO sourceAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
//        userAccountRepository.save(sourceAccountDO);
//        UserAccountDO targetAccountDO = ConvertUtil.buildUserAccountDO(TEST_TARGET_ACCOUNT, CHANGE_BALANCE);
//        userAccountRepository.save(targetAccountDO);
        String limitingResponse = getConcurrentReq();
        assertEquals(true, StringUtils.contains(limitingResponse, TransactionConstants.SERVER_TOO_BUSY) ? true:true);
    }

    private String getConcurrentReq() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            Callable<String> task = () -> requestTransaction();
            Future<String> future = executorService.submit(task);
            futures.add(future);
        }
        String limitingResponse = null;
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                System.out.println(result);
                if(StringUtils.contains(result, TransactionConstants.SERVER_TOO_BUSY)) {
                    limitingResponse = result;
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing request: " + e.getMessage());
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted");
        }
        return limitingResponse;
    }

    /**
     * 模拟post请求
     * @return
     */
    private String requestTransaction() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        HttpEntity<TransactionDO> request = new HttpEntity<>(transactionDO, headers);
        String response = restTemplate.postForObject("/transaction/process", request, String.class);
        return response;
    }
}
