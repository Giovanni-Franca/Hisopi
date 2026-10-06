package Hisopi.Hisopi.infra.security;

public class RateLimitConfig {

    private final int maxRequests;
    private final long windowMillis;

    public RateLimitConfig(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowMillis() {
        return windowMillis;
    }
}