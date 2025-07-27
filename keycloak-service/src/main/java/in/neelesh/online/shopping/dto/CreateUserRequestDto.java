package in.neelesh.online.shopping.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequestDto {

	private String userId;
	private String username;
	String realm;
	private String email;
	private String password;
	private boolean enabled = true;
	private boolean emailVerified = false;;
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private String address;
	private String postalCode;
	private String city;
	private String state;
	private String country;
	private String customerSupportNumber;

}
