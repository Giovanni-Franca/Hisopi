package Hisopi.Hisopi.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static class RequestInfo {
        private int requests;
        private long startTime;

        public RequestInfo() {
            this.requests = 0;
            this.startTime = System.currentTimeMillis();
        }
    }

    private final Map<String, RequestInfo> requests = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_MILLIS = 60_000;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        RequestInfo requestInfo = requests.computeIfAbsent(
                clientIp,
                key -> new RequestInfo()
        );

        synchronized (requestInfo) {

            long currentTime = System.currentTimeMillis();

            // Reinicia a janela de 1 minuto
            if (currentTime - requestInfo.startTime >= WINDOW_MILLIS) {
                requestInfo.requests = 0;
                requestInfo.startTime = currentTime;
            }

            requestInfo.requests++;

            // Limite atingido
            if (requestInfo.requests > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente mais tarde.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}