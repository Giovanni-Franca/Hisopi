package Hisopi.Hisopi.infra.ApiKey;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import Hisopi.Hisopi.services.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

	@Autowired
	ApiKeyService apiKeyService;

	@Bean
	FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter f) {
	    var reg = new FilterRegistrationBean<>(f);
	    reg.setEnabled(false);
	    return reg;
	}	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {

	    var chave = request.getHeader("X-API-Key");

	    if (chave != null && !chave.isBlank()) {
	        var principal = apiKeyService.validar(chave.trim());

	        if (principal.isEmpty()) {
	            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            response.setContentType("application/json");
	            response.getWriter().write("{\"erro\":\"API Key inválida ou revogada\"}");
	            return; // chave informada e inválida = 401, sem seguir a cadeia
	        }

	        SecurityContextHolder.getContext()
	                .setAuthentication(new ApiKeyAuthenticationToken(principal.get()));
	    }

	    filterChain.doFilter(request, response);
	}
}