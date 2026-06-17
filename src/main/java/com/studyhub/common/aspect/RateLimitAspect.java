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
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_PREFIX = "studyhub:ratelimit:";

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public RateLimitAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return joinPoint.proceed();
        }

        String key = parseKey(rateLimit.key(), joinPoint);
        String rateLimitKey = RATE_LIMIT_PREFIX + key + ":" + userId;

        long now = System.currentTimeMillis();
        long windowStart = now - (rateLimit.window() * 1000L);

        stringRedisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, windowStart);

        Long count = stringRedisTemplate.opsForZSet().zCard(rateLimitKey);

        if (count != null && count >= rateLimit.limit()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, rateLimit.message());
        }

        stringRedisTemplate.opsForZSet().add(rateLimitKey, String.valueOf(now), now);

        stringRedisTemplate.expire(rateLimitKey, rateLimit.window() * 2, TimeUnit.SECONDS);

        return joinPoint.proceed();
    }

    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        if (key == null || key.isBlank()) {
            return joinPoint.getSignature().toShortString();
        }

        if (!key.startsWith("#")) {
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
