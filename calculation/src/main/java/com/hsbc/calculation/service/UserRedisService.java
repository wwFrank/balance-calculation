package com.hsbc.calculation.service;

import com.hsbc.calculation.domain.UserAccountDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserRedisService {
    private static final Logger logger = LoggerFactory.getLogger(UserRedisService.class);

    @Autowired
    private RedisTemplate<String, UserAccountDO> redisTemplate;

    /**
     * 从缓存中读取用户余额
     * @param accountNumber
     * @return
     */
    public UserAccountDO getUserAccountFromCache(String accountNumber) {
        try {
            return redisTemplate.opsForValue().get(accountNumber);
        } catch (Exception e) {
            logger.warn("getUserBalanceFromCache null:userAccountNumber={}", accountNumber);
            return null;
        }
    }

    /**
     * 更新缓存，如果异常记录日志
     * @param accountNumber
     * @param accountDO
     * @return
     */
    public void updateUserAccount(String accountNumber, UserAccountDO accountDO) {
        try {
            redisTemplate.opsForValue().set(accountNumber, accountDO);
        } catch (Exception e) {
            logger.warn("UserRedisService calls updateUserAccount error:userAccountNumber={}", accountNumber);
        }
    }
}
