package in.neelesh.online.shopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class KeycloakConfig {

	@Value("${keycloak.token-url}")
	private String tokenUrl;

	@Value("${keycloak.resource}")
	private String keycloakResource;

	@Value("${keycloak.admin-client}")
	private String adminClient;

	@Value("${keycloak.admin-credentials.username}")
	private String adminUsername;

	@Value("${keycloak.admin-credentials.password}")
	private String adminPassword;

	@Value("${keycloak.user-name-url}")
	private String userNameUrl;

	@Value("${keycloak.credentials.secret}")
	private String getCredentialsSecret;

	@Value("${keycloak.logout-user-url}")
	private String getUserByUserIdFromNotificationLogoutUrl;

	@Value("${keycloak.logout-url}")
	private String getUserLogoutFromDashboardUrl;

	@Value("${keycloak.get-userid-by-username-url}")
	private String getUserIdByUsernameUrl;

	@Value("${keycloak.execute-actions-email-url}")
	private String getExecuteActionsEmailUrl;

	@Value("${keycloak.create-role-url}")
	private String createKeycloakRole;

	@Value("${keycloak.create-user-url}")
	private String createUserUrl;

	@Value("${keycloak.assign-role-url}")
	private String assignRoleToUser;

	@Value("${keycloak.get-roles-url}")
	private String getListOfRoles;

	@Value("${keycloak.get-role-by-name-url}")
	private String getRoleByRoleNameUrl;

	@Value("${keycloak.update-user-url}")
	private String updateUserUrl;

	@Value("${keycloak.delete-role-url}")
	private String deleteRole;
	
	@Value("${keycloak.delete-user-url}")
	private String deleteUserUrl;
	
	@Value("${keycloak.get-user-by-id-url}")
	private String getUserByUserIdUrl;

}
