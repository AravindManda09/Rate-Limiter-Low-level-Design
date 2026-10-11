public class RateLimitConfig {
    private final int timeWindowInSeconds;
    private final int maxRequests;

    public RateLimitConfig(int timeWindowInSeconds, int maxRequests) {
        this.maxRequests = maxRequests;
        this.timeWindowInSeconds = timeWindowInSeconds;
    }
    public int getTimeWindowInSeconds() { return timeWindowInSeconds; }
    public int getMaxRequests() { return maxRequests; }
}
