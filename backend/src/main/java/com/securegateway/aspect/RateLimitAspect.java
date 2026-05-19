package com.securegateway.aspect;

import com.securegateway.annotation.RateLimited;
import com.securegateway.model.RateLimitLog;
import com.securegateway.repository.RateLimitLogRepository;
import com.securegateway.service.TokenBucketService;
import com.securegateway.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class RateLimitAspect {

    private final TokenBucketService tokenBucketService;
    private final RateLimitLogRepository logRepository;

    // Standard Constructor (No Lombok)
    public RateLimitAspect(TokenBucketService tokenBucketService, RateLimitLogRepository logRepository) {
        this.tokenBucketService = tokenBucketService;
        this.logRepository = logRepository;
    }

    @Before("@annotation(rateLimited)")
    public void checkRateLimit(RateLimited rateLimited) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String clientIp = request.getRemoteAddr();

        boolean isBlocked = false;
        
        System.out.println("AOP TRIGGERED: " + clientIp);

        try {
            tokenBucketService.consume(clientIp, rateLimited.capacity(), rateLimited.refillRate());
        } catch (RateLimitException e) {
            isBlocked = true;
            throw e; // Re-throw so the user gets the 429 error
        } finally {
            // This is the "Audit" part - Always log the attempt to MySQL
            RateLimitLog log = new RateLimitLog(clientIp, LocalDateTime.now(), isBlocked);
            logRepository.save(log);
        }
    }
}