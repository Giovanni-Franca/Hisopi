package Hisopi.Hisopi.infra.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import Hisopi.Hisopi.model.Usuario;
import jakarta.annotation.PostConstruct;

@Service
public class TokenService {
	
	@Value("${api.security.token.secret}")
	private String secret;
	private Algorithm algorithm;
	@PostConstruct
	private void init() {
		this.algorithm = Algorithm.HMAC256(secret);
	}
	
	public String generateToken(Usuario user) {
		try {
			String token = JWT.create()
				.withIssuer("auth-api")
				.withSubject(user.getEmail())
				.withExpiresAt(getExpirationDate())
				.withClaim("type", "access")
				.sign(algorithm);
			return token;
		} catch(JWTCreationException exception) {
			throw new RuntimeException("Error while generating token", exception);
		}
	}
	
	public String generateRefreshToken(Usuario user) {
		try {
			String token = JWT.create()
				.withIssuer("auth-api")
				.withSubject(user.getEmail())
				.withExpiresAt(getExpirationDateRefresh())
				.withClaim("type", "refresh")
				.sign(algorithm);
			return token;
		} catch(JWTCreationException exception) {
			throw new RuntimeException("Error while generating token", exception);
		}
	}
	
	public String validate(String token, String expectedType) {
		try {
	        DecodedJWT decoded = JWT.require(algorithm)
	        		.withIssuer("auth-api")
	        		.build()
	        		.verify(token);
	        String type = decoded.getClaim("type").asString();
	        if (!expectedType.equals(type)) {
	        	return "";
	        }
	        return decoded.getSubject();
		} catch(JWTVerificationException exception) {
			return "";
		}
	}
	
	public String validateToken(String token) {
	    return validate(token, "access");
	}

	public String validateRefreshToken(String token) {
	    return validate(token, "refresh");
	}
	
	private Instant getExpirationDate() {
		return Instant.now().plus(15,ChronoUnit.MINUTES);
	}
	
	private Instant getExpirationDateRefresh() {
		return Instant.now().plus(7,ChronoUnit.DAYS);
	}

}
