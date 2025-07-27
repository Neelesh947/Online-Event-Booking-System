package in.online.event.booking.event.config;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final int REALM_SEGMENT_INDEX = 1;

	@Value("${keycloak.initial.jwks.url}")
	private String initialJwksUrl;

	@Value("${keycloak.final.jwks.url}")
	private String finalJwksUrl;

	@Autowired
	private CustomAuthorizationErrorHandler errorHandler;

	/**
	 * Extracts the realm segment from the request URI to build the JWKS URL
	 * dynamically.
	 */
	private final Function<HttpServletRequest, String> jwksUriExtractor = request -> {
		var pathSegments = request.getRequestURI().split("/");
		if (pathSegments.length <= REALM_SEGMENT_INDEX) {
			throw new IllegalArgumentException("Invalid URI: Realm segment missing");
		}
		return initialJwksUrl + pathSegments[REALM_SEGMENT_INDEX] + finalJwksUrl;
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configure(http)).csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.exceptionHandling(exceptions -> exceptions.accessDeniedHandler(errorHandler));

		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		return token -> {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			String jwksUri = jwksUriExtractor.apply(request);
			System.out.println("JWKS URI resolved to: " + jwksUri);
			return NimbusJwtDecoder.withJwkSetUri(jwksUri).build().decode(token);
		};
	}

	@SuppressWarnings("unchecked")
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Object realmAccessClaim = jwt.getClaim("realm_access");
			Map<String, Object> realmAccess = (Map<String, Object>) realmAccessClaim;
			List<String> roles = ExtractAuthorityFromJwt.extractRoles((realmAccess));
			return roles.stream().map(role -> "ROLE_" + role.toUpperCase()).map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
		});
		return converter;
	}

	public CustomAuthenticationConverterForClaims authenticationConverterForClaims() {
		return new CustomAuthenticationConverterForClaims();
	}
}
