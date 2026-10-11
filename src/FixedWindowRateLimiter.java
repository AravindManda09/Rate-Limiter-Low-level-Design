import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter extends RateLimiter {

    // 1. Combine the state into a single inner class
    private static class WindowState {
        long windowStart;
        int count;

        WindowState(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }

    // 2. Use a single map tracking the combined state per user
    private final ConcurrentHashMap<String, WindowState> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(RateLimitConfig config) {
        super(config);
    }

    @Override
    public boolean allowRequest(String userId) {
        long currentTimeSec = System.currentTimeMillis() / 1000;
        long windowSize = config.getTimeWindowInSeconds();
        long currentWindowStart = (currentTimeSec / windowSize) * windowSize;

        // Array to extract the boolean result out of the lambda safely
        boolean[] isAllowed = new boolean[1];

        // 3. Lock ONLY this specific user's state
        windows.compute(userId, (key, currentState) -> {

            // Scenario A: It's a brand-new window (or their first request ever)
            if (currentState == null || currentState.windowStart != currentWindowStart) {
                isAllowed[0] = true;
                // Return a fresh state object. compute() saves this in the map.
                return new WindowState(currentWindowStart, 1);
            }

            // Scenario B: We are still in the current window
            else {
                if (currentState.count < config.getMaxRequests()) {
                    isAllowed[0] = true;
                    currentState.count++;
                    return currentState; // Save the incremented state
                } else {
                    isAllowed[0] = false;
                    return currentState; // Save the state as-is (do not increment)
                }
            }
        });

        return isAllowed[0];
    }
}