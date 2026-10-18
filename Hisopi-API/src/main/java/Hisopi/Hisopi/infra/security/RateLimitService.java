package Hisopi.Hisopi.infra.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static class RequestInfo {
        private int requests;
        private long startTime;

        public RequestInfo() {
            this.requests = 0;
            this.startTime = System.currentTimeMillis();
        }
    }

    private final Map<String, RequestInfo> requests = new ConcurrentHashMap<>();

    private final Map<String, RateLimitConfig> limits = Map.of(
            "/auth/login", new RateLimitConfig(5, 180_000),
            "/auth/register", new RateLimitConfig(5, 600_000),
            "/auth/refresh", new RateLimitConfig(10, 180_000),
            "/auth/me", new RateLimitConfig(30, 60_000)
    );

    private final RateLimitConfig defaultConfig =
            new RateLimitConfig(60, 60_000);

    public boolean isAllowed(String clientIp, String endpoint) {

        RateLimitConfig config = limits.getOrDefault(endpoint, defaultConfig);

        String key = clientIp + ":" + endpoint;

        RequestInfo requestInfo = requests.computeIfAbsent(
                key,
                k -> new RequestInfo()
        );

        synchronized (requestInfo) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - requestInfo.startTime >= config.getWindowMillis()) {
                requestInfo.requests = 0;
                requestInfo.startTime = currentTime;
            }

            requestInfo.requests++;
            return requestInfo.requests <= config.getMaxRequests();
        }
    }

}