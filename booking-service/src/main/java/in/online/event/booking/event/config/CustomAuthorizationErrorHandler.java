package in.online.event.booking.event.config;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component

public class CustomAuthorizationErrorHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		String errorMessage = "Authorization Failed due to Restricted access: " + accessDeniedException.getMessage();
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter().write(errorMessage);
		response.getWriter().flush();
		response.getWriter().close();
	}

}
