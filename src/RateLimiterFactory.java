// RateLimiterFactory.java
public class RateLimiterFactory {
    public static RateLimiter createRateLimiter(UserTier tier, RateLimitConfig config) {
        // Feature of Strategy & Factory pattern: change/assign any algorithm depending on tiers
        switch (tier) {
            case FREE:
                return new TokenBucketRateLimiter(config); // Strict / burst handling
            case PRO:
                return new FixedWindowRateLimiter(config); // Generous / simple windows
            default:
                throw new IllegalArgumentException("Unsupported user tier");
        }
    }
}
