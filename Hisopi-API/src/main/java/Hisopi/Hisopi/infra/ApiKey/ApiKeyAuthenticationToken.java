package Hisopi.Hisopi.infra.ApiKey;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final ApiKeyPrincipal principal;

    public ApiKeyAuthenticationToken(ApiKeyPrincipal principal) {
        super(List.of(new SimpleGrantedAuthority("ROLE_API_KEY")));
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override public Object getCredentials() { return null; }
    @Override public ApiKeyPrincipal getPrincipal() { return principal; }
}