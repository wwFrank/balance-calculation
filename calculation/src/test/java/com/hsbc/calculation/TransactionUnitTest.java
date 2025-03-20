package com.hsbc.calculation;

import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.domain.UserAccountDO;
import com.hsbc.calculation.repository.TransactionRepository;
import com.hsbc.calculation.repository.UserAccountRepository;
import com.hsbc.calculation.service.UserRedisService;
import com.hsbc.calculation.utils.ConvertUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Optional;

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

    @Autowired
    private RedisTemplate<String, UserAccountDO> redisTemplate;

    private UserAccountDO insertAccountToDB() {
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userAccountRepository.save(userAccountDO);
        return userAccountDO;
    }

    /**
     * 测试缓存中查询用户余额
     */
    @Test
    public void testQueryAccountFromCache() {
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO userBalanceFromCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        assertEquals(true, userBalanceFromCache != null);
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

    /**
     * 测试更新缓存中用户余额
     */
    @Test
    public void testUpdateAccountBalanceInCache() {
        UserAccountDO userAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO userAccountFromCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        assertEquals(INI_BALANCE, userAccountFromCache != null ? userAccountFromCache.getBalance().intValue() : 0);

        userAccountFromCache.setBalance(new BigDecimal(CHANGE_BALANCE));
        userRedisService.updateUserAccount(userAccountDO.getAccountNumber(), userAccountDO);
        UserAccountDO updateUserAccountInCache = userRedisService.getUserAccountFromCache(userAccountDO.getAccountNumber());
        assertEquals(CHANGE_BALANCE, updateUserAccountInCache != null ? updateUserAccountInCache.getBalance().intValue() : 0);
    }

    @Test
    public void testSaveTransaction() {
        UserAccountDO sourceAccountDO = ConvertUtil.buildUserAccountDO(TEST_ACCOUNT_NUMBER, INI_BALANCE);
        userAccountRepository.save(sourceAccountDO);
        UserAccountDO targetAccountDO = ConvertUtil.buildUserAccountDO(TEST_TARGET_ACCOUNT, INI_BALANCE);
        userAccountRepository.save(targetAccountDO);
        TransactionDO transactionDO = ConvertUtil.buildTransactionDO(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_NUMBER, 100);
        TransactionDO save = transactionRepository.save(transactionDO);
        assertEquals(save, save != null);
    }
}
