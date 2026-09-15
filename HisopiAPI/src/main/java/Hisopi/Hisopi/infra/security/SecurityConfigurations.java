package Hisopi.Hisopi.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {
	
	@Autowired
	SecurityFilter securityFilter;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
		return httpSecurity
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
						.requestMatchers(HttpMethod.POST, "/endpoint").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/espacos/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/espacos/*").permitAll()
						.requestMatchers(HttpMethod.PUT, "/espacos/*").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/espacos/*").permitAll()
						
						.requestMatchers(HttpMethod.GET, "/espacos/{idEspaco}/insumos/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/espacos/{idEspaco}/insumos/*").permitAll()
						.requestMatchers(HttpMethod.PUT, "/espacos/{idEspaco}/insumos/*").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/espacos/{idEspaco}/insumos/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/espacos/{idEspaco}/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/espacos/{idEspaco}/*").permitAll()
						.requestMatchers(HttpMethod.PUT, "/espacos/{idEspaco}/*").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/espacos/{idEspaco}/*").permitAll()
						
						.requestMatchers(HttpMethod.GET, "/espacos/{idEspaco}/receitas/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/espacos/{idEspaco}/receitas/*").permitAll()
						.requestMatchers(HttpMethod.PUT, "/espacos/{idEspaco}/receitas/*").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/espacos/{idEspaco}/receitas/*").permitAll()
						.anyRequest().authenticated()
					)
				.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
