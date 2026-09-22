package com.studyhub.common.aspect;

import com.studyhub.common.ErrorCode;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.common.annotation.RateLimit;
import com.studyhub.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Aspect
@Component
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_PREFIX = "studyhub:ratelimit:";
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>("""
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])
            local count = redis.call('ZCARD', KEYS[1])
            if count >= tonumber(ARGV[2]) then return 0 end
            redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4])
            redis.call('EXPIRE', KEYS[1], ARGV[5])
            return 1
            """, Long.class);

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public RateLimitAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Long userId = LoginUserHolder.getUserId();
        String key = parseKey(rateLimit.key(), joinPoint);
        String subject = userId == null ? "anonymous" : String.valueOf(userId);
        String rateLimitKey = RATE_LIMIT_PREFIX + key + ":" + subject;

        long now = System.currentTimeMillis();
        long windowStart = now - (rateLimit.window() * 1000L);

        Long allowed = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(rateLimitKey),
                String.valueOf(windowStart),
                String.valueOf(rateLimit.limit()),
                String.valueOf(now),
                now + ":" + UUID.randomUUID(),
                String.valueOf(rateLimit.window() * 2)
        );
        if (!Long.valueOf(1L).equals(allowed)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        if (key == null || key.isBlank()) {
            return joinPoint.getSignature().toShortString();
        }

        if (!key.contains("#")) {
            return key;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

        if (paramNames == null) {
            return key;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(key);
        return String.valueOf(expression.getValue(context));
    }
}
