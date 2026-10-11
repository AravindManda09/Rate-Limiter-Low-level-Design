// TokenBucketRateLimiter.java
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter extends RateLimiter {
    // Map of User -> current token count
    private final ConcurrentHashMap<String, Double> tokens = new ConcurrentHashMap<>();
    // Map of User -> timestamp of the last token refill (ms)
    private final ConcurrentHashMap<String, Long> lastRefillTime = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(RateLimitConfig config) {
        super(config);
    }

    @Override
    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();

        // We use a 1-element array to safely extract the result out of the lambda
        boolean[] isAllowed = new boolean[1];

        tokens.compute(userId, (key, currentTokenCount) -> {
            long lastRefill = lastRefillTime.getOrDefault(userId, currentTime);
            double maxTokens = config.getMaxRequests();
            double refillRatePerMs = (double) config.getMaxRequests() / (config.getTimeWindowInSeconds() * 1000.0);

            // 1. Calculate and add newly accumulated tokens
            double currentTokens = currentTokenCount == null ? maxTokens : currentTokenCount;
            long elapsedTime = currentTime - lastRefill;
            double refilledTokens = elapsedTime * refillRatePerMs;

            currentTokens = Math.min(maxTokens, currentTokens + refilledTokens);

            // Updating a DIFFERENT map is perfectly safe inside compute
            lastRefillTime.put(userId, currentTime);

            // 2. Evaluate request
            if (currentTokens >= 1.0) {
                isAllowed[0] = true;
                return currentTokens - 1.0; // compute automatically saves this new balance!
            } else {
                isAllowed[0] = false;
                return currentTokens; // We return their current balance so the key isn't deleted
            }
        });

        // Return the result we captured inside the lambda
        return isAllowed[0];
    }
}