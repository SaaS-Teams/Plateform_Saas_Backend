package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.domain.services.RateLimitingService;

@Service
@RequiredArgsConstructor
public class RateLimitingServiceImpl implements RateLimitingService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isAllowed(String key, int maxPerMinute) {
        String redisKey = "rate_limit:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, Duration.ofMinutes(1));
        }
        
        return count != null && count <= maxPerMinute;
    }
}
