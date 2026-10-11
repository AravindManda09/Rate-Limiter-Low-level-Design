// Main.java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimiterService rateLimiterService = new RateLimiterService();

        // 1. Configure Tiers
        // Free tier: 3 requests per 5 seconds
        rateLimiterService.registerTierConfig(UserTier.FREE, new RateLimitConfig(5, 3));
        // Premium tier: 10 requests per 5 seconds
        rateLimiterService.registerTierConfig(UserTier.PRO,new RateLimitConfig(5, 10));

        // 2. Setup parallel execution to verify thread safety
        ExecutorService executor = Executors.newFixedThreadPool(15);
        String userId = "user_abc_123";

        System.out.println("--- Free User Triggers 8 Parallel Requests (Limit: 3) ---");
        for (int i = 1; i <= 8; i++) {
            final int requestNum = i;
            executor.submit(() -> {
                boolean allowed = rateLimiterService.allowRequest(userId, UserTier.PRO);
                System.out.printf("Request #%d: %s\n", requestNum, allowed ? "ALLOWED (200 OK)" : "BLOCKED (429 Too Many Requests)");
            });
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }
}