package com.hsbc.calculation.limiting;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户限流服务
 * Guava RateLimiter来实现限流
 * 每个用户有一个独立的RateLimiter实例，每秒最多允许1个请求
 */
@Service
public class RateLimitingService {
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    public boolean allowRequest(String userId) {
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(userId, k -> RateLimiter.create(1.0)); // 1 request per second
        return rateLimiter.tryAcquire();
    }
}
