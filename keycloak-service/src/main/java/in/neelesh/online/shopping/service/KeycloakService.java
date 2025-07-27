package in.neelesh.online.shopping.service;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

import in.neelesh.online.shopping.dto.CreateUserRequestDto;
import in.neelesh.online.shopping.dto.KeyCloakTokenResponseDto;
import in.neelesh.online.shopping.dto.UserCredentialsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface KeycloakService {

	KeyCloakTokenResponseDto loginUser(@Valid UserCredentialsDto userCredentialsDto, String realm);

	public ResponseEntity<?> sendForgetPasswordToEmail(String username, String realm);

	ResponseEntity<?> createRole(String realm, String role);

	CreateUserRequestDto createKeycloakUsersAndAssignRoles(CreateUserRequestDto createUserRequestDto, String realm,
			String role);

	void assignRoleToUser(String realm, String userId, String role);

	UserRepresentation updateKeycloakUser(UserRepresentation keyCloakRepresentation,
			@NotNull(message = "User ID must not be null or empty") String userId,
			@NotNull(message = "Realm must not be null or empty") String realm);

	public void enableUserForConfirmationWhileCreatingTheAcocunt(String realm, String userId);
	
	public List<Map<String, Object>> getRoleList(String realm);
	public Map<String, Object> getRole(String realm, String roleName);
	
	public ResponseEntity<?> getUserIdByUserName(String username, String realm) ;

	ResponseEntity<?> deleteRoleByRoleName(String roleName, String realm);

	ResponseEntity<?> deleteUserByUserId(String userId, String realm);
	
	public void logoutUserFromDashboard(String userId, String realm);
	
	public ResponseEntity<?> refreshAccessToken(String refreshToken, String realm);

}
