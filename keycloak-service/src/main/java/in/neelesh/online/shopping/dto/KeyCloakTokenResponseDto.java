package in.neelesh.online.shopping.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(content = Include.NON_NULL)
public class KeyCloakTokenResponseDto {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_in")
	private int expiresIn;

	@JsonProperty("refresh_expires_in")
	private int refreshExpiresIn;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("not-before-policy")
	private long notBeforePolicy;

	@JsonProperty("session_state")
	private String sessionState;

	private String scope;

	@JsonProperty("error")
	private String error;

	@JsonProperty("error_description")
	private String message;
}