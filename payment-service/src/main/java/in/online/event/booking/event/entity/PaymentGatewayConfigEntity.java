package in.online.event.booking.event.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import in.online.event.booking.event.enums.ActivationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "paymentRecords" })
@EqualsAndHashCode(callSuper = true, exclude = { "paymentRecords" })
@Table(name = "payment_gateway_config")
public class PaymentGatewayConfigEntity extends BaseEntity {

	@NotBlank(message = "Gateway name should not be empty")
	@Size(max = 100, message = "Gateway name must be at most 100 characters")
	@Pattern(regexp = "^(?i)(razorpay|stripe|paypal)$", message = "Gateway name must be one of: razorpay, stripe, paypal")
	@Column(name = "gateway_name", nullable = false, length = 100)
	private String gatewayName;

	@NotBlank(message = "API key must not be blank")
	@Size(max = 255, message = "API key must be less than or equal to 255 characters")
	@Column(name = "api_key", nullable = false, length = 255)
	private String apiKey;

	@NotBlank(message = "API secret must not be blank")
	@Size(max = 255, message = "API secret must be less than or equal to 255 characters")
	@Column(name = "api_secret", nullable = false, length = 255)
	private String apiSecret;

	@NotBlank(message = "Environment must not be blank")
	@Size(max = 50, message = "Environment must be less than or equal to 50 characters")
	@Column(name = "environment", nullable = false, length = 50)
	private String environment;

	@Enumerated(EnumType.STRING)
	@Column(name = "activation_status", nullable = false, length = 20)
	private ActivationStatus activationStatus;

	@Lob
	@Column(name = "additional_details", columnDefinition = "TEXT")
	private String additionalDetails;

	@Size(max = 150, message = "Merchant name must be less than or equal to 150 characters")
	@Column(name = "merchant_name", length = 150)
	private String merchantName;

	@Size(max = 2048, message = "Webhook URL must be less than or equal to 2048 characters")
	@URL(message = "Webhook URL must be valid url")
	@Column(name = "webhook_url")
	private String webhookUrl;

	@Size(max = 255, message = "Webhook secret must be less than or equal to 255 characters")
	@Column(name = "webhook_secret", length = 255)
	private String webhookSecret;

	@OneToMany(mappedBy = "paymentGatewayConfigEntity", fetch = FetchType.LAZY)
	@Default
	private List<Payment> paymentRecords = new ArrayList<>();
}
