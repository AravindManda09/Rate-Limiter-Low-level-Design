Thread-Safe Extensible Rate Limiter (LLD)

A robust, production-grade Low-Level Design (LLD) implementation of a Rate Limiter in Java. Designed with a focus on thread-safety, lock-striping via ConcurrentHashMap, and high extensibility using object-oriented design patterns.

📐 Architecture & UML Class Diagram

The system separates concerns across layers—decoupling configuration rules from the execution engines, and isolating concurrency per individual user ID without global thread contention.

classDiagram
direction LR
class User {
- String id
- UserTier tier
}
class UserTier {
<<enumeration>>
FREE
PREMIUM
}
class RateLimitConfig {
- int timeWindowInSeconds
- int maxRequests
+ getTimeWindowInSeconds() int
+ getMaxRequests() int
}
class RateLimiter {
<<abstract>>
# RateLimitConfig config
+ allowRequest(String userId)* boolean
}
class TokenBucketRateLimiter {
- ConcurrentHashMap tokens
- ConcurrentHashMap lastRefillTime
+ allowRequest(String userId) boolean
}
class FixedWindowRateLimiter {
- ConcurrentHashMap windows
+ allowRequest(String userId) boolean
}
class RateLimiterFactory {
+ createRateLimiter(UserTier tier, RateLimitConfig config) RateLimiter
}
class RateLimiterService {
- ConcurrentHashMap limiters
+ registerTierConfig(UserTier tier, RateLimitConfig config)
+ allowRequest(String userId, UserTier tier) boolean
}

    RateLimiter <|-- TokenBucketRateLimiter
    RateLimiter <|-- FixedWindowRateLimiter
    RateLimiterService o-- RateLimiter
    RateLimiterFactory ..> RateLimiter : creates
    User --> UserTier
    RateLimiter --> RateLimitConfig


🏗️ Design Patterns & Components

Strategy Pattern (RateLimiter): Defines a common abstract contract (allowRequest(userId)), allowing multiple underlying algorithms to be plugged in interchangeably.

Factory Pattern (RateLimiterFactory): Centralizes object creation and decouples the orchestration layer from concrete algorithm instantiation.

Service Orchestrator (RateLimiterService): Acts as the entry point, mapping different user tiers to their respective rate-limiting engines.

Configuration Separation (RateLimitConfig): Isolates business rules (time windows and max limits) from the algorithmic math, enabling dynamic re-configurations.

⚡ Supported Algorithms & Concurrency Strategy

Token Bucket (TokenBucketRateLimiter):

Handles smooth request bursts by continuously refilling tokens based on elapsed time since the user's last request.

Uses per-user atomic updates via ConcurrentHashMap.compute() to avoid global synchronization bottlenecks.

Fixed Window (FixedWindowRateLimiter):

Tracks requests in discretized time blocks.

Bundles window timestamps and request counters into an immutable WindowState object to guarantee atomic updates without race conditions.

🚀 Quick Start / Usage Example

public class Main {
public static void main(String[] args) {
RateLimiterService service = new RateLimiterService();

        // 1. Register Tier Configurations
        // Free Tier: 3 requests per 5-second window
        service.registerTierConfig(UserTier.FREE, new RateLimitConfig(5, 3));
        
        // Premium Tier: 10 requests per 5-second window
        service.registerTierConfig(UserTier.PREMIUM, new RateLimitConfig(5, 10));

        // 2. Check Requests
        String userId = "user_abc_123";
        
        boolean isAllowed = service.allowRequest(userId, UserTier.FREE);
        if (isAllowed) {
            System.out.println("Request successful (200 OK)");
        } else {
            System.out.println("Rate limited (429 Too Many Requests)");
        }
    }