package in.online.event.booking.event.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {

	private SecurityUtils() {
	}

	public static final Supplier<Authentication> authenticationSupplier = () -> SecurityContextHolder.getContext()
			.getAuthentication();

	public static final Supplier<String> getCurrentUserIdSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof Jwt principal) {
			return principal.getClaim(SecurityHelperUtils.SUB);
		}
		return null;
	};

	public static final Supplier<String> getCurrentUserUsernameSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttribute(SecurityHelperUtils.PREFERED_USERNAME);
		}
		return null;
	};

	public static final Supplier<String> getCurrentNameSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttribute(SecurityHelperUtils.NAME);
		}
		return null;
	};

	public static final Supplier<String> getCurrentUserFirstNameSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttribute(SecurityHelperUtils.GIVEN_NAME);
		}
		return null;
	};

	public static final Supplier<String> getCurrentUserLastNameSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttribute(SecurityHelperUtils.FAMILY_NAME);
		}
		return null;
	};

	public static final Supplier<String> getCurrentUserEmailSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttribute(SecurityHelperUtils.EMAIL);
		}
		return null;
	};

	public static final Supplier<List<String>> getCurrentUserRoleSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
		}
		return Collections.emptyList();
	};

	public static final Supplier<Collection<? extends GrantedAuthority>> getCurrentUserAuthoritiesSupplier = () -> {
		Authentication authentication = authenticationSupplier.get();
		if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAuthorities();
		}
		return null;
	};
}
