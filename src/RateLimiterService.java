
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterService {
    private final ConcurrentHashMap<UserTier, RateLimiter> limiters = new ConcurrentHashMap<>();

    public void registerTierConfig(UserTier tier, RateLimitConfig config) {
        limiters.put(tier, RateLimiterFactory.createRateLimiter(tier, config));
    }
    public boolean allowRequest(String userId, UserTier tier) {
        RateLimiter limiter = limiters.get(tier);
        if (limiter == null) {
            // Default Fallback
            return true;
        }
        return limiter.allowRequest(userId);
    }
}