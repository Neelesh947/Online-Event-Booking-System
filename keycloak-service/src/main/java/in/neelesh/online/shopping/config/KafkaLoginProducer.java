package in.neelesh.online.shopping.config;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import in.neelesh.online.shopping.dto.CreateUserRequestDto;
import in.neelesh.online.shopping.records.AccountActivation;
import in.neelesh.online.shopping.records.AccountDeletion;
import in.neelesh.online.shopping.records.LoginConfirmation;
import in.neelesh.online.shopping.records.Welcome;
import in.neelesh.online.shopping.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLoginProducer {

	private final KafkaTemplate<String, LoginConfirmation> kafkaTemplate;
	private final KafkaTemplate<String, AccountActivation> accountActivationKafkatemplate;
	private final KafkaTemplate<String, Welcome> welcomeKafkaTemplate;
	private final KafkaTemplate<String, AccountDeletion> accountDeletionKafkaTemplate;

	@Async
	public void sendLoginConfirmation(LoginConfirmation loginConfirmation) {
		log.info("Sending Login Confirmation...");
		Message<LoginConfirmation> message = MessageBuilder.withPayload(loginConfirmation)
				.setHeader(KafkaHeaders.TOPIC, Constants.LOGIN_TOPIC).build();
		kafkaTemplate.send(message);
		log.info("Login confirmation sent successfully for user: {}: {}", message, loginConfirmation.userName());
	}

	@Async
	public void sendAccountCreatedSuccessfully(CreateUserRequestDto createUserRequestDto) {
		log.info("Sending account activation email for user: {}", createUserRequestDto.getUsername());
		String activationLink = "http://localhost:1234/Shopping/update/user/" + createUserRequestDto.getUserId();
		AccountActivation accountActivation = new AccountActivation(createUserRequestDto.getUserId(),
				createUserRequestDto.getUsername(), createUserRequestDto.getEmail(),
				createUserRequestDto.getFirstName(), activationLink);

		Message<AccountActivation> message = MessageBuilder.withPayload(accountActivation)
				.setHeader(KafkaHeaders.TOPIC, Constants.USER_CREATED_CONFIRMATION).build();
		accountActivationKafkatemplate.send(message);
		log.info("Account activation email sent to Kafka for user: {}", createUserRequestDto.getUsername());

	}

	@Async
	public void sendWelcomeMessage(CreateUserRequestDto createUserRequestDto) {
		log.info("Sending welcome email for user: {}", createUserRequestDto.getUsername());
		Welcome welcome = new Welcome(createUserRequestDto.getUserId(), createUserRequestDto.getUsername(),
				createUserRequestDto.getEmail(), createUserRequestDto.getRealm());

		Message<Welcome> message = MessageBuilder.withPayload(welcome)
				.setHeader(KafkaHeaders.TOPIC, Constants.WELCOME_MESSAGE).build();
		welcomeKafkaTemplate.send(message);
		log.info("Account welcome email sent to Kafka for user: {}", createUserRequestDto.getUsername());

	}

	@Async
	public void sendUserDeletionEmail(AccountDeletion accountDeletion) {
		log.info("Sending account deletion mail to emal: {} and username: {}", accountDeletion.email(),
				accountDeletion.username());
		AccountDeletion deletion = new AccountDeletion(accountDeletion.email(), accountDeletion.username());
		Message<AccountDeletion> message = MessageBuilder.withPayload(deletion)
				.setHeader(KafkaHeaders.TOPIC, Constants.ACCOUNT_DELETION).build();
		accountDeletionKafkaTemplate.send(message);
		log.info("Account deatctivation email successfully sent for user", accountDeletion.username());
	}
}
