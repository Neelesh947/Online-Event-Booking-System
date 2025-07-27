package in.neelesh.online.shopping.records;

import java.time.LocalDateTime;

public record LoginConfirmation(String userName, String email, String realm, LocalDateTime loginTime, String message) {

}
