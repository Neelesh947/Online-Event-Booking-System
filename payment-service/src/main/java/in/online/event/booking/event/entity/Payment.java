package in.online.event.booking.event.entity;

import in.online.event.booking.event.enums.PaymentGateway;
import in.online.event.booking.event.enums.PaymentMode;
import in.online.event.booking.event.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@ToString(exclude = { "paymentGatewayConfigEntity" })
@EqualsAndHashCode(callSuper = false, exclude = { "paymentGatewayConfigEntity" })
@Table(name = "payment_records")
@Builder
public class Payment extends BaseEntity {

	@NotNull(message = "Payment Gateway is mandatory")
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_gateway", nullable = false)
	private PaymentGateway paymentGateway;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_gateway_config_id")
	private PaymentGatewayConfigEntity paymentGatewayConfigEntity;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_mode")
	private PaymentMode paymentMode;

	@NotNull(message = "Amount is required")
	@Column(name = "amount_in_paise", nullable = false)
	private Integer amountInPaise;

	@Column(name = "external_payment_id")
	private String externalPaymentId;

	@Column(name = "payment_reference_url")
	private String paymentReferenceUrl;

	@NotNull(message = "Payment status is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false)
	private PaymentStatus paymentStatus;

	@Column(name = "transaction_id")
	private String transactionId;

	@Column(name = "upi_request_id")
	private String upiRequestId;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "event_Id")
	private String eventId;
}
