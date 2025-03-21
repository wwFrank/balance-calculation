package com.hsbc.calculation.intercepter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.calculation.constants.TransactionConstants;
import com.hsbc.calculation.domain.TransactionDO;
import com.hsbc.calculation.limiting.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 创建一个拦截器来限流
 * 通过请求中的 userId 参数来检查限流器是否允许请求通过
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RateLimitingService rateLimitingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = getUserId(request);
        if (userId != null && !rateLimitingService.allowRequest(userId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write(TransactionConstants.SERVER_TOO_BUSY);
            return false;
        }
        return true;
    }

    /**
     * 这里为POST请求限流，从请求体中获取userId
     * @param request
     * @return
     */
    private String getUserId(HttpServletRequest request) {
        if(HttpMethod.POST.name().equals(request.getMethod())) {
            try {
                StringBuilder requestBody = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
                TransactionDO transactionDO = objectMapper.readValue(requestBody.toString(), TransactionDO.class);
                if(transactionDO != null) {
                    return transactionDO.getSourceAccountNumber();
                }
            } catch (IOException e) {
            }
        }
        return null;


    }
}
