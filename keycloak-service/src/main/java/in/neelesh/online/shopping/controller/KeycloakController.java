package in.neelesh.online.shopping.controller;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.neelesh.online.shopping.dto.CreateUserRequestDto;
import in.neelesh.online.shopping.dto.KeyCloakTokenResponseDto;
import in.neelesh.online.shopping.dto.UserCredentialsDto;
import in.neelesh.online.shopping.service.KeycloakService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/{realm}")
@CrossOrigin("*")
public class KeycloakController {

	private final KeycloakService keycloakService;

	@PostMapping("/login")
	public KeyCloakTokenResponseDto userLogin(@RequestBody UserCredentialsDto userCredentialsDto,
			@PathVariable String realm) {
		userCredentialsDto.setRealm(realm);
		KeyCloakTokenResponseDto response = keycloakService.loginUser(userCredentialsDto, realm);
		log.info("successfully login to user: {}, with response:{}", userCredentialsDto.getUserName(), response);
		return response;
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> sendForgetPasswordToEmail(@RequestParam String username, @PathVariable String realm) {
		return keycloakService.sendForgetPasswordToEmail(username, realm);
	}

	@PostMapping("/create-role")
	public ResponseEntity<?> createRole(@PathVariable String realm, @RequestParam String role) {
		return keycloakService.createRole(realm, role);
	}

	@PostMapping("/create-user/{role}")
	public ResponseEntity<?> createUserInKeycloak(@RequestBody CreateUserRequestDto createUserRequestDto,
			@PathVariable String realm, @PathVariable String role) {
		CreateUserRequestDto users = keycloakService.createKeycloakUsersAndAssignRoles(createUserRequestDto, realm,
				role);
		return ResponseEntity.status(HttpStatus.CREATED).body(users);
	}

	@PostMapping("/assign-role/{userId}/{role}")
	public ResponseEntity<?> assignRoleToTheKaycloakUser(@PathVariable String realm, @PathVariable String userId,
			@PathVariable String role) {
		try {
			keycloakService.assignRoleToUser(realm, userId, role);
			return ResponseEntity.ok(Map.of("status", "success", "message",
					"Role '" + role + "' assigned to user '" + userId + "' in realm '" + realm + "'"));
		} catch (Exception e) {
			log.error("Failed to assign role", e);
			return ResponseEntity.internalServerError()
					.body(Map.of("status", "error", "message", "Failed to assign role: " + e.getMessage()));
		}
	}

	@PutMapping("/update/user/{userId}")
	public ResponseEntity<UserRepresentation> updateKeycloakUser(@RequestBody UserRepresentation keyCloakRepresentation,
			@NotNull(message = "User ID must not be null or empty") @PathVariable String userId,
			@NotNull(message = "Realm must not be null or empty") @PathVariable String realm) {
		log.debug("Updating user for userId: {} in realm: {}", userId, realm);
		try {
			UserRepresentation updateUser = keycloakService.updateKeycloakUser(keyCloakRepresentation, userId, realm);
			return ResponseEntity.ok(updateUser);
		} catch (Exception e) {
			log.error("user update failed for userId: {}", userId);
			throw e;
		}
	}

	@PatchMapping("/update/user/{userId}")
	public ResponseEntity<String> activateUser(@PathVariable String userId, @PathVariable String realm) {
		try {
			this.keycloakService.enableUserForConfirmationWhileCreatingTheAcocunt(realm, userId);
			return ResponseEntity.ok("User account activated successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to activate user account: " + e.getMessage());
		}
	}

	@GetMapping("/roles")
	public ResponseEntity<?> getRolesByRealm(@PathVariable String realm) {
		try {
			List<Map<String, Object>> roles = keycloakService.getRoleList(realm);
			return ResponseEntity.ok(roles);
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("error", "Failed to fetch roles", "message", e.getMessage()));
		}
	}

	@GetMapping("/{roleName}")
	public ResponseEntity<?> getRoleByName(@PathVariable String realm, @PathVariable String roleName) {
		try {
			Map<String, Object> role = keycloakService.getRole(realm, roleName);
			return ResponseEntity.ok(role);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch role", "details", e.getMessage()));
		}
	}

	@GetMapping("/user-id")
	public ResponseEntity<?> getUserIdByUsername(@PathVariable String username, @PathVariable String realm) {
		return keycloakService.getUserIdByUserName(username, realm);
	}

	@DeleteMapping("/role/{roleName}")
	public ResponseEntity<?> deleteRole(@PathVariable String roleName, @PathVariable String realm) {
		return this.keycloakService.deleteRoleByRoleName(roleName, realm);
	}

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<?> deleteUserByUserId(@PathVariable String userId, @PathVariable String realm) {
		return this.keycloakService.deleteUserByUserId(userId, realm);
	}

	@PostMapping("/logout/{userId}")
	public ResponseEntity<String> logoutUserFromDashboard(@PathVariable String realm, @PathVariable String userId) {
		try {
			keycloakService.logoutUserFromDashboard(userId, realm);
			return ResponseEntity.ok("User logged out successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to logout user: " + e.getMessage());
		}
	}
	
	@PostMapping("/refresh-token")
	public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload,@PathVariable String realm){
		String refreshToken = payload.get("refresh_token");
		return this.keycloakService.refreshAccessToken(refreshToken, realm);
	}
}
