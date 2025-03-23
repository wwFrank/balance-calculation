package com.hsbc.calculation.service;

import com.alibaba.fastjson.JSON;
import com.hsbc.calculation.domain.UserAccountDO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserRedisService {
    private static final Logger logger = LoggerFactory.getLogger(UserRedisService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 从缓存中读取用户余额
     * @param accountNumber
     * @return
     */
    public UserAccountDO getUserAccountFromCache(String accountNumber) {
        try {
            String json = redisTemplate.opsForValue().get(accountNumber);
            if(StringUtils.isNotBlank(json)) {
                return JSON.parseObject(json, UserAccountDO.class);
            }
        } catch (Exception e) {
            logger.warn("getUserBalanceFromCache null:userAccountNumber={}", accountNumber, e);
        }
        return null;
    }

    /**
     * 更新缓存，如果异常记录日志
     * @param accountNumber
     * @param accountDO
     * @return
     */
    public void updateUserAccount(String accountNumber, UserAccountDO accountDO) {
        try {
            redisTemplate.opsForValue().set(accountNumber, JSON.toJSONString(accountDO));
        } catch (Exception e) {
            logger.warn("UserRedisService calls updateUserAccount error:userAccountNumber={}", accountNumber, e);
        }
    }
}
