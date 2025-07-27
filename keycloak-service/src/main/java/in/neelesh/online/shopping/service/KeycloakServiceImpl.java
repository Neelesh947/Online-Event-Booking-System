package in.neelesh.online.shopping.service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.InternalException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import in.neelesh.online.shopping.config.KafkaLoginProducer;
import in.neelesh.online.shopping.config.KeycloakConfig;
import in.neelesh.online.shopping.dto.CreateUserRequestDto;
import in.neelesh.online.shopping.dto.ErrorResponseDto;
import in.neelesh.online.shopping.dto.KeyCloakTokenResponseDto;
import in.neelesh.online.shopping.dto.UserCredentialsDto;
import in.neelesh.online.shopping.records.AccountDeletion;
import in.neelesh.online.shopping.records.LoginConfirmation;
import in.neelesh.online.shopping.utils.Constants;
import in.neelesh.online.shopping.utils.ErrorConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

	private final KeycloakConfig keycloakConfig;
	private final RestTemplate restTemplate;
	private final KafkaLoginProducer kafkaLoginProducer;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public KeyCloakTokenResponseDto loginUser(@Valid UserCredentialsDto userCredentialsDto, String realm) {
		String tokenUrl = keycloakConfig.getTokenUrl().replace("{0}", realm);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add(Constants.CLIENT_SECRET, keycloakConfig.getGetCredentialsSecret());
		body.add(Constants.CLIENT_ID, keycloakConfig.getKeycloakResource());
		body.add(Constants.GRANT_TYPE, Constants.PASSWORD);
		body.add(Constants.USERNAME, userCredentialsDto.getUserName());
		body.add(Constants.PASSWORD, userCredentialsDto.getPassword());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});
			Map<String, Object> responseBody = response.getBody();
			KeyCloakTokenResponseDto tokenResponse = mapToTokenResponse(responseBody);
			String email = extractEMailFromToken(tokenResponse.getAccessToken());

			kafkaLoginProducer.sendLoginConfirmation(new LoginConfirmation(userCredentialsDto.getUserName(), email,
					realm, LocalDateTime.now(), Constants.USER_LOGIN_SUCCESS));

			return tokenResponse;
		} catch (Exception e) {
			throw new RuntimeException("Failed to authenticate with Keycloak: " + e.getMessage(), e);
		}
	}

	private String extractEMailFromToken(String accessToken) throws ParseException {
		SignedJWT signedJWT = SignedJWT.parse(accessToken);
		return (String) signedJWT.getJWTClaimsSet().getClaim("email");
	}

	private KeyCloakTokenResponseDto mapToTokenResponse(Map<String, Object> responseBody) {
		if (responseBody == null) {
			throw new RuntimeException("Empty or invalid response from Keycloak");
		}

		KeyCloakTokenResponseDto tokenResponse = new KeyCloakTokenResponseDto();
		tokenResponse.setAccessToken((String) responseBody.get("access_token"));
		tokenResponse.setExpiresIn((Integer) responseBody.get("expires_in"));
		tokenResponse.setRefreshExpiresIn((Integer) responseBody.get("refresh_expires_in"));
		tokenResponse.setRefreshToken((String) responseBody.get("refresh_token"));
		tokenResponse.setTokenType((String) responseBody.get("token_type"));
		tokenResponse.setNotBeforePolicy((Integer) responseBody.get("not-before-policy"));
		tokenResponse.setSessionState((String) responseBody.get("session_state"));
		tokenResponse.setScope((String) responseBody.get("scope"));

		return tokenResponse;
	}

	private ResponseEntity<KeyCloakTokenResponseDto> getAdminUsernameFromKeycloak() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(Constants.USERNAME, keycloakConfig.getAdminUsername());
		map.add(Constants.PASSWORD, keycloakConfig.getAdminPassword());
		map.add(Constants.CLIENT_ID, keycloakConfig.getAdminClient());
		map.add(Constants.GRANT_TYPE, Constants.PASSWORD);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		String tokenUrl = keycloakConfig.getTokenUrl().replace("{0}", "master");
		return restTemplate.postForEntity(tokenUrl, request, KeyCloakTokenResponseDto.class);
	}

	public ResponseEntity<?> sendForgetPasswordToEmail(String username, String realm) {
		ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
		if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("status", "error", "message", "Failed to get admin token from Keycloak"));
		}
		KeyCloakTokenResponseDto token = accessTokenResponse.getBody();
		ResponseEntity<?> userIdResponse = getUserIdByUsername(username, token, realm);
		if (!userIdResponse.getStatusCode().is2xxSuccessful()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("status", "error", "message", "User not found for username: " + username));
		}
		String userId = (String) userIdResponse.getBody();
		return sendResetPasswordEmail(realm, userId, token.getAccessToken());
	}

	// Helper method to get user ID from KeyCloak
	private ResponseEntity<?> getUserIdByUsername(String username, KeyCloakTokenResponseDto token, String realm) {
		String url = MessageFormat.format(keycloakConfig.getGetUserIdByUsernameUrl(), realm, username);
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccessToken());
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Void> request = new HttpEntity<>(headers);
		try {
			ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, request,
					new ParameterizedTypeReference<List<Map<String, Object>>>() {
					});
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null
					|| response.getBody().isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
			}

			String userId = (String) response.getBody().get(0).get("id");
			return ResponseEntity.ok(userId);
		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body("Failed to fetch user ID: " + e.getMessage());
		}
	}

	private ResponseEntity<?> sendResetPasswordEmail(String realm, String userId, String accessToken) {
		String url = MessageFormat.format(keycloakConfig.getGetExecuteActionsEmailUrl(), realm, userId);
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		List<String> actions = List.of("UPDATE_PASSWORD");
		HttpEntity<List<String>> emailRequest = new HttpEntity<>(actions, headers);

		try {
			ResponseEntity<Void> emailResponse = restTemplate.exchange(url, HttpMethod.PUT, emailRequest, Void.class);

			if (emailResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
				return ResponseEntity.ok(Map.of("status", "success", "message", "Password reset email sent."));
			} else {
				return ResponseEntity.status(emailResponse.getStatusCode())
						.body(Map.of("status", "error", "message", "Failed to send password reset email"));
			}

		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode())
					.body(Map.of("status", "error", "message", "Keycloak error: " + e.getMessage()));
		}
	}

	public ResponseEntity<?> createRole(String realm, String role) {
		ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
		if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("status", "error", "message", "Failed to get admin token from Keycloak"));
		}
		KeyCloakTokenResponseDto token = accessTokenResponse.getBody();
		String url = MessageFormat.format(keycloakConfig.getCreateKeycloakRole(), realm);
		Map<String, Object> payload = Map.of(Constants.NAME, role);
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccessToken());
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode())
					.body(Map.of("status", "error", "message", e.getResponseBodyAsString()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

	public CreateUserRequestDto createKeycloakUsersAndAssignRoles(CreateUserRequestDto createUserRequestDto,
			String realm, String roleName) {
		ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
		if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token from Keycloak");
		}
		KeyCloakTokenResponseDto token = accessTokenResponse.getBody();
		String url = MessageFormat.format(keycloakConfig.getCreateUserUrl(), realm);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token.getAccessToken());

		UserRepresentation userRepresentation = createUserRepresentationFromDto(
				getCredentialRepresentations(createUserRequestDto), createUserRequestDto,
				getUserAttributes(createUserRequestDto, listOfRoles(roleName)));

		HttpEntity<UserRepresentation> entity = new HttpEntity<>(userRepresentation, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.postForEntity(url, entity, String.class);
		} catch (HttpClientErrorException e) {
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException(ErrorConstants.SOME_THING_WENT_WRONG);
		}

		if (response.getStatusCode() == HttpStatus.CREATED) {
			ResponseEntity<?> userIdResponse = getUserIdByUsername(createUserRequestDto.getUsername(), token, realm);
			String userId = userIdResponse.getBody().toString();
			Map<String, Object> roles = getRole(realm, roleName);
			String roleGet = (String) roles.get(Constants.NAME);
			assignRoleToUser(realm, userId, roleGet);
			createUserRequestDto.setUserId(userId);
			kafkaLoginProducer.sendAccountCreatedSuccessfully(createUserRequestDto);
			createUserRequestDto.setRealm(realm);
			kafkaLoginProducer.sendWelcomeMessage(createUserRequestDto);
			return createUserRequestDto;
		} else {
			throw new RuntimeException("Failed to create user: " + response.getStatusCode());
		}
	}

	private List<String> listOfRoles(String roleName) {
		List<String> roleList = new ArrayList<>();
		roleList.add(roleName);
		return roleList;
	}

	private List<CredentialRepresentation> getCredentialRepresentations(CreateUserRequestDto keyCloakRepresentation) {
		List<CredentialRepresentation> credentailsList = new ArrayList<>();
		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setTemporary(false);
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(keyCloakRepresentation.getPassword());
		credentailsList.add(credentialRepresentation);
		return credentailsList;
	}

	private Map<String, List<String>> getUserAttributes(CreateUserRequestDto keyCloakRepresentation,
			List<String> roleList) {
		Map<String, List<String>> attributeList = new HashMap<>();
		// Add roles to the attribute list
		attributeList.put("roles", roleList);
		// Helper function to add attributes if they are not null or empty
		addAttribute(attributeList, "phoneNumber", keyCloakRepresentation.getPhoneNumber());
		addAttribute(attributeList, "address", keyCloakRepresentation.getAddress());
		addAttribute(attributeList, "city", keyCloakRepresentation.getCity());
		addAttribute(attributeList, "postalCode", keyCloakRepresentation.getPostalCode());
		addAttribute(attributeList, "state", keyCloakRepresentation.getState());
		addAttribute(attributeList, "country", keyCloakRepresentation.getCountry());
		addAttribute(attributeList, "customerSupportNumber", keyCloakRepresentation.getCustomerSupportNumber());
		return attributeList;
	}

	private void addAttribute(Map<String, List<String>> attributeList, String key, String value) {
		if (value != null && !value.isEmpty()) {
			attributeList.put(key, Collections.singletonList(value));
		}
	}

	private UserRepresentation createUserRepresentationFromDto(List<CredentialRepresentation> credentialsList,
			CreateUserRequestDto createUserRequestDto, Map<String, List<String>> attributeList) {

		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setFirstName(createUserRequestDto.getFirstName());
		userRepresentation.setLastName(createUserRequestDto.getLastName());
		userRepresentation.setEmailVerified(false);
		userRepresentation.setEnabled(true);
		userRepresentation.setUsername(createUserRequestDto.getUsername());
		userRepresentation.setEmail(createUserRequestDto.getEmail());
		userRepresentation.setCredentials(credentialsList);
		userRepresentation.setAttributes(attributeList);

		return userRepresentation;
	}

	public List<Map<String, Object>> getRoleList(String realm) {
		try {
			ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
			if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
				throw new IllegalStateException("Unable to retrieve admin token from Keycloak");
			}

			KeyCloakTokenResponseDto token = accessTokenResponse.getBody();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(token.getAccessToken());
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Assuming you have a URL template in keycloakConfig to get all roles
			String getRoleUrl = MessageFormat.format(keycloakConfig.getGetListOfRoles(), realm);

			ResponseEntity<List<Map<String, Object>>> roleResponse = restTemplate.exchange(getRoleUrl, HttpMethod.GET,
					new HttpEntity<>(headers), new ParameterizedTypeReference<List<Map<String, Object>>>() {
					});

			if (!roleResponse.getStatusCode().is2xxSuccessful() || roleResponse.getBody() == null) {
				throw new IllegalStateException("Failed to fetch roles for realm: " + realm);
			}

			return roleResponse.getBody();

		} catch (Exception e) {
			throw new RuntimeException("Failed to get role list for realm: " + realm, e);
		}
	}

	public Map<String, Object> getRole(String realm, String roleName) {
		ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
		if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
			throw new IllegalStateException("Unable to retrieve admin token from Keycloak");
		}

		KeyCloakTokenResponseDto token = accessTokenResponse.getBody();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccessToken());
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Assuming your URL template supports role name, e.g., /{realm}/roles/{role}
		String url = MessageFormat.format(keycloakConfig.getGetRoleByRoleNameUrl(), realm, roleName);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET,
				new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, Object>>() {
				});

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("Failed to fetch role: " + roleName);
		}

		return response.getBody();
	}

	public void assignRoleToUser(String realm, String userId, String roleName) {
		try {
			ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
			if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
				throw new IllegalStateException("Unable to retrieve admin token from Keycloak");
			}

			KeyCloakTokenResponseDto token = accessTokenResponse.getBody();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(token.getAccessToken());
			headers.setContentType(MediaType.APPLICATION_JSON);

			String assignRoleUrl = MessageFormat.format(keycloakConfig.getAssignRoleToUser(), realm, userId);

			Map<String, Object> role = getRole(realm, roleName);
			if (role == null) {
				throw new IllegalStateException("Role not found: " + roleName);
			}

			List<Map<String, Object>> rolesToAssign = Collections.singletonList(role);
			HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(rolesToAssign, headers);

			restTemplate.exchange(assignRoleUrl, HttpMethod.POST, request, Void.class);

		} catch (Exception e) {
			throw new RuntimeException("Role assignment failed for userId: " + userId + " in realm: " + realm, e);
		}
	}

	@Override
	public UserRepresentation updateKeycloakUser(UserRepresentation keyCloakRepresentation,
			@NotNull(message = "User ID must not be null or empty") String userId,
			@NotNull(message = "Realm must not be null or empty") String realm) {

		ResponseEntity<KeyCloakTokenResponseDto> accessTokenResponse = getAdminUsernameFromKeycloak();
		if (!accessTokenResponse.getStatusCode().is2xxSuccessful() || accessTokenResponse.getBody() == null) {
			throw new IllegalStateException("Unable to retrieve admin token from Keycloak");
		}

		KeyCloakTokenResponseDto token = accessTokenResponse.getBody();

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccessToken());
		headers.setContentType(MediaType.APPLICATION_JSON);

		String url = MessageFormat.format(keycloakConfig.getUpdateUserUrl(), realm, userId);
		String requestBody = convertToJson(keyCloakRepresentation);

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<UserRepresentation> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
					UserRepresentation.class);
			HttpStatusCode statusCode = response.getStatusCode();
			if (statusCode.is4xxClientError() || statusCode.is5xxServerError()) {
				String responseString = convertToJson(response.getBody());
				ErrorResponseDto errorResponse = OBJECT_MAPPER.readValue(responseString, ErrorResponseDto.class);
				throw new RuntimeException("Error updating user: " + errorResponse.getErrorMessage());
			}
			return response.getBody();
		} catch (Exception e) {
			throw new InternalException("not able to update the user", e);
		}
	}

	private String convertToJson(UserRepresentation keyCloakRepresentation) {
		try {
			return OBJECT_MAPPER.writeValueAsString(keyCloakRepresentation);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to convert KeyCloakRepresentation to JSON", e);
		}
	}

	public void enableUserForConfirmationWhileCreatingTheAcocunt(String realm, String userId) {
		ResponseEntity<KeyCloakTokenResponseDto> tokenResponse = getAdminUsernameFromKeycloak();
		if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token");
		}
		String accessToken = tokenResponse.getBody().getAccessToken();

		String url = MessageFormat.format(keycloakConfig.getUpdateUserUrl(), realm, userId);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// JSON body to enable user
		Map<String, Object> body = new HashMap<>();
		body.put("enabled", true);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

		restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
	}

	public ResponseEntity<?> getUserIdByUserName(String username, String realm) {
		ResponseEntity<KeyCloakTokenResponseDto> tokenResponse = getAdminUsernameFromKeycloak();
		if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token");
		}
		String accessToken = tokenResponse.getBody().getAccessToken();
		String url = MessageFormat.format(keycloakConfig.getGetUserIdByUsernameUrl(), realm, username);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET,
					requestEntity, new ParameterizedTypeReference<List<Map<String, Object>>>() {
					});

			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null
					|| response.getBody().isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in realm: " + realm);
			}

			Map<String, Object> userInfo = response.getBody().get(0); // Assuming first match
			String userId = (String) userInfo.get(Constants.ID);

			return ResponseEntity
					.ok(Map.of("status", "success", "userId", userId, "username", username, "realm", realm));

		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body("Keycloak error: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
		}
	}

	public ResponseEntity<?> deleteRoleByRoleName(String roleName, String realm) {
		ResponseEntity<KeyCloakTokenResponseDto> tokenResponse = getAdminUsernameFromKeycloak();
		if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token");
		}
		String accessToken = tokenResponse.getBody().getAccessToken();
		String url = MessageFormat.format(keycloakConfig.getDeleteRole(), realm, roleName);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		try {
			restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Role successfully deleted"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
		}
	}

	public ResponseEntity<?> deleteUserByUserId(String userId, String realm) {
		ResponseEntity<KeyCloakTokenResponseDto> tokenResponse = getAdminUsernameFromKeycloak();
		if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token");
		}
		String accessToken = tokenResponse.getBody().getAccessToken();
		String url = MessageFormat.format(keycloakConfig.getDeleteUserUrl(), realm, userId);

		ResponseEntity<Map<String, Object>> getUserDetails = getUserByUserId(realm, userId);
		Map<String, Object> user = getUserDetails.getBody();
		String username = (String) user.get(Constants.USERNAME);
		String email = (String) user.get(Constants.EMAIL);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		try {
			restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
			if (email != null && !email.isEmpty()) {
				AccountDeletion deletion = new AccountDeletion(email, username);
				kafkaLoginProducer.sendUserDeletionEmail(deletion);
			}
			return ResponseEntity.ok("User deleted successfully. Notification sent to: " + email);
		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body("Error during user deletion: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
		}
	}

	public ResponseEntity<Map<String, Object>> getUserByUserId(String realm, String userId) {
		ResponseEntity<KeyCloakTokenResponseDto> tokenResponse = getAdminUsernameFromKeycloak();
		if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token");
		}
		String accessToken = tokenResponse.getBody().getAccessToken();
		String url = MessageFormat.format(keycloakConfig.getGetUserByUserIdUrl(), realm, userId);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					new ParameterizedTypeReference<>() {
					});
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
			}

			return ResponseEntity.ok(response.getBody());
		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", "Keycloak error: " + e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Unexpected error: " + e.getMessage()));
		}
	}

	public void logoutUserFromDashboard(String userId, String realm) {
		ResponseEntity<KeyCloakTokenResponseDto> tokenResponse = getAdminUsernameFromKeycloak();
		if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
			throw new RuntimeException("Failed to get admin token");
		}
		String accessToken = tokenResponse.getBody().getAccessToken();
		String url = MessageFormat.format(keycloakConfig.getGetUserLogoutFromDashboardUrl(), realm);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException ex) {
			throw ex;
		} catch (Exception e) {
			throw e;
		}
	}

	public ResponseEntity<?> refreshAccessToken(String refreshToken, String realm) {
		String tokenUrl = keycloakConfig.getTokenUrl().replace("{0}", realm);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add(Constants.CLIENT_ID, keycloakConfig.getKeycloakResource());
		body.add(Constants.CLIENT_SECRET, keycloakConfig.getGetCredentialsSecret());
		body.add(Constants.GRANT_TYPE, Constants.REFRESH_TOKEN);
		body.add(Constants.REFRESH_TOKEN, refreshToken);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			Map<String, Object> responseBody = response.getBody();
			if (responseBody == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empty response from Keycloak");
			}

			KeyCloakTokenResponseDto tokenResponse = mapToTokenResponse(responseBody);
			return ResponseEntity.ok(tokenResponse);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Unexpected error during token refresh: " + e.getMessage());
		}
	}
}
