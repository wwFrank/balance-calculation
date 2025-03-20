package com.hsbc.calculation.intercepter;

import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.limiting.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 创建一个拦截器来限流
 * 通过请求中的 userId 参数来检查限流器是否允许请求通过
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimitingService rateLimitingService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getParameter(TransactionConstants.INTERCEPTOR_PARAM);
        if (userId == null || !rateLimitingService.allowRequest(userId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write(TransactionConstants.SERVER_TOO_BUSY);
            return false;
        }
        return true;
    }
}
