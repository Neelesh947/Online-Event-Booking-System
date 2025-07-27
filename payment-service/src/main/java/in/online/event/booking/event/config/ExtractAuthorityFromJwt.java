package in.online.event.booking.event.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

public class ExtractAuthorityFromJwt {

	private ExtractAuthorityFromJwt() {
	}
	
	public static JwtAuthenticationConverter createCustomJwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(ExtractAuthorityFromJwt::extractAuthorities);
		return converter;
	}
	
	public static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		List<String> roles = extractRoles(jwt.getClaim("realm_access"));
		return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public static List<String> extractRoles(Map<String, Object> realmAccess) {
		List<String> allRoles = (List<String>) realmAccess.get("roles");
		List<String> rolesToSkip = Arrays.asList("", "", "");
		List<String> roleList = new ArrayList<>();
		for (String role : allRoles) {
			if (!rolesToSkip.contains(role)) {
				roleList.add(role);
			}
		}
		return roleList;
	}
}
