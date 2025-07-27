package in.online.event.booking.event.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public class CustomAuthenticationConverterForClaims implements Converter<Jwt, AbstractAuthenticationToken>{

	@Override
	public AbstractAuthenticationToken convert(Jwt source) {
		// TODO Auto-generated method stub
		return null;
	}

}
