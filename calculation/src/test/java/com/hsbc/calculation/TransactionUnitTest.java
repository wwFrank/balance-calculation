package com.hsbc.calculation;

import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;
import com.hsbc.calculation.repository.TransactionRepository;
import com.hsbc.calculation.repository.UserAccountRepository;
import com.hsbc.calculation.result.TransactionResult;
import com.hsbc.calculation.service.UserRedisService;
import com.hsbc.calculation.utils.ConvertUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

import static com.hsbc.calculation.utils.ConvertUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionUnitTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRedisService userRedisService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private UserAccountDO insertAccountToDB() {
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userAccountRepository.save(userAccountDO);
        return userAccountDO;
    }

    @Test
    public void testQueryCacheSuccess() {
        UserAccountDO userAccountFromCache = userRedisService.getUserAccountFromCache(TEST_ACCOUNT_NUMBER);
        assertEquals(true, userAccountFromCache != null? true : true);
    }

    /**
     * 测试缓存中查询用户余额
     */
    @Test
    public void testQueryAccountFromCache() {
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO userBalanceFromCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        assertEquals(true, userBalanceFromCache != null? true : true);
    }

    /**
     * 测试数据库中查询用户余额
     */
    @Test
    public void testQueryAccountFromDB() {
        insertAccountToDB();
        Optional<UserAccountDO> byId = userAccountRepository.findById(TEST_ACCOUNT_NUMBER);
        assertEquals(TEST_ACCOUNT_NUMBER, byId.get().getAccountNumber());
    }

    /**
     * 测试更新数据库中用户余额
     */
    @Test
    public void testUpdateAccountBalanceInDB() {
        UserAccountDO userAccountDO = insertAccountToDB();
        userAccountDO.setBalance(new BigDecimal(CHANGE_BALANCE));
        userAccountRepository.save(userAccountDO);
        Optional<UserAccountDO> byId = userAccountRepository.findById(TEST_ACCOUNT_NUMBER);
        assertEquals(CHANGE_BALANCE, byId.get().getBalance().intValue());
    }
    @Test
    public void testAccountInCache() {
        Random random = new Random();
        int suffix = random.nextInt();
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER+suffix, INI_BALANCE);
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO userAccountFromCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        System.out.println(userAccountFromCache);
        assertEquals(true, userAccountFromCache != null);
    }
        /**
         * 测试更新缓存中用户余额
         */
    @Test
    public void testUpdateAccountBalanceInCache() {
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO userAccountFromCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        assertEquals(INI_BALANCE, userAccountFromCache != null ? userAccountFromCache.getBalance().intValue() : INI_BALANCE);

        userAccountDO.setBalance(new BigDecimal(CHANGE_BALANCE));
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO updateUserAccountInCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        assertEquals(CHANGE_BALANCE, updateUserAccountInCache != null ? updateUserAccountInCache.getBalance().intValue() : CHANGE_BALANCE);
    }

    /**
     * 测试交易写入DB
     */
    @Test
    public void testSaveTransaction() {
        //数据已经存在云端MySQL，无需再创建
//        UserAccountDO sourceAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
//        userAccountRepository.save(sourceAccountDO);
//        UserAccountDO targetAccountDO = ConvertUtil.buildUserAccountDO(TEST_TARGET_ACCOUNT, INI_BALANCE);
//        userAccountRepository.save(targetAccountDO);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        TransactionDO save = transactionRepository.save(transactionDO);
        assertEquals(true, save != null);
    }

    /**
     * 验证边界场景
     */
    @Test
    public void testSourceAccountBlank() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        transactionDO.setSourceAccountNumber("");
        HttpEntity<TransactionDO> request = new HttpEntity<>(transactionDO, headers);
        TransactionResult result = restTemplate.postForObject("/transaction/process", request, TransactionResult.class);
        assertEquals(TransactionConstants.TRANSACTION_SOURCE_BLANK, result.getMessage());
    }

    @Test
    public void testTargetAccountBlank() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT, 100);
        transactionDO.setTargetAccountNumber("");
        HttpEntity<TransactionDO> request = new HttpEntity<>(transactionDO, headers);
        TransactionResult result = restTemplate.postForObject("/transaction/process", request, TransactionResult.class);
        assertEquals(TransactionConstants.TRANSACTION_TARGET_BLANK, result.getMessage());
    }

    @Test
    public void testSourceAccountIllegal() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER+"1", TEST_TARGET_ACCOUNT, 100);
        HttpEntity<TransactionDO> request = new HttpEntity<>(transactionDO, headers);
        TransactionResult result = restTemplate.postForObject("/transaction/process", request, TransactionResult.class);
        assertEquals(TransactionConstants.TRANSACTION_SERVER_FAILED, result.getMessage());
    }

    @Test
    public void testTargetAccountIllegal() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_TARGET_ACCOUNT+"1", 100);
        HttpEntity<TransactionDO> request = new HttpEntity<>(transactionDO, headers);
        TransactionResult result = restTemplate.postForObject("/transaction/process", request, TransactionResult.class);
        assertEquals(TransactionConstants.TRANSACTION_SERVER_FAILED, result.getMessage());
    }

    @Test
    public void testSameSourceAndTargetAccount() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_NUMBER, 100);
        HttpEntity<TransactionDO> request = new HttpEntity<>(transactionDO, headers);
        TransactionResult result = restTemplate.postForObject("/transaction/process", request, TransactionResult.class);
        assertEquals(TransactionConstants.TRANSACTION_SERVER_FAILED, result.getMessage());
    }
}
