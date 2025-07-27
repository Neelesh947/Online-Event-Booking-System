package in.online.event.booking.event.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.online.event.booking.event.entity.Payment;
import in.online.event.booking.event.enums.PaymentMode;
import in.online.event.booking.event.enums.PaymentStatus;
import in.online.event.booking.event.repository.PaymentRepository;
import in.online.event.booking.event.utils.RazorPaySignatureVerify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {

	@Value("${webhook.secret}")
	private String webhookSecret;

	private final ObjectMapper objectMapper;
	private final RazorPaySignatureVerify razorPaySignatureVerify;

	private final PaymentRepository paymentRepository;

	@SuppressWarnings("static-access")
	@Override
	@Transactional
	public void processWebhook(String payload, String razorpaySignature) {
		try {
			log.info("Webhook payload: {}", payload);
			log.info("Razorpay signature header: {}", razorpaySignature);

			boolean isValid = razorPaySignatureVerify.isSignatureValid(payload, razorpaySignature, webhookSecret);

			if (!isValid) {
				return;
			}

			JsonNode rootNode = parsePayload(payload);
			String eventType = rootNode.get("event").asText();

			switch (eventType) {
			case "payment.captured":
				handlePaymentCaptured(rootNode);
				break;
			case "payment.failed":
				handlePaymentFailed(rootNode);
				break;
			case "order.paid":
				handleOrderPaid(rootNode);
				break;
			default:
				log.warn("⚠️ Unhandled webhook event type: {}", eventType);
			}

		} catch (Exception e) {
			log.error("🔥 Error processing Razorpay webhook: {}", e.getMessage(), e);
		}
	}

	private JsonNode parsePayload(String payload) throws JsonProcessingException {
		return objectMapper.readTree(payload);
	}

	private void handlePaymentCaptured(JsonNode rootNode) {
		JsonNode entity = rootNode.at("/payload/payment/entity");
		String paymentId = entity.get("id").asText();
		String orderId = entity.get("order_id").asText();
		long amount = entity.get("amount").asLong();
		String method = entity.get("method").asText();

		log.info("💰 Payment Captured | paymentId: {}, orderId: {}, amount: {}", paymentId, orderId, amount);
		Payment paymentRecord = paymentRepository.findByTransactionId(orderId);

		if (paymentRecord == null) {
			log.warn("Payment record not found for orderId: {}", orderId);
			return;
		}

		paymentRecord.setExternalPaymentId(paymentId);
		paymentRecord.setPaymentStatus(PaymentStatus.SUCCESS);
		paymentRecord.setAmountInPaise((int) amount); // cast as needed
		paymentRecord.setTransactionId(orderId);
		paymentRecord.setPaymentMode(method != null ? mapToPaymentMode(method) : null);

		paymentRepository.save(paymentRecord);
	}

	private void handlePaymentFailed(JsonNode rootNode) {
		JsonNode entity = rootNode.at("/payload/payment/entity");
		String paymentId = entity.get("id").asText();
		String orderId = entity.get("order_id").asText();
		String errorReason = entity.has("error_description") ? entity.get("error_description").asText() : "Unknown";

		log.warn("❌ Payment Failed | paymentId: {}, reason: {}", paymentId, errorReason);
		Payment paymentRecord = paymentRepository.findByTransactionId(orderId);
		if (paymentRecord == null) {
			log.warn("Payment record not found for orderId: {}", orderId);
			return;
		}

		paymentRecord.setExternalPaymentId(paymentId);
		paymentRecord.setPaymentStatus(PaymentStatus.FAILED);

		paymentRepository.save(paymentRecord);
	}

	private void handleOrderPaid(JsonNode rootNode) {
		JsonNode entity = rootNode.at("/payload/order/entity");
		String razorpayOrderId = entity.get("id").asText();

		log.info("🧾 Order Paid | razorpayOrderId: {}", razorpayOrderId);

		Payment paymentRecord = paymentRepository.findByTransactionId(razorpayOrderId);
		if (paymentRecord != null) {
			paymentRecord.setPaymentStatus(PaymentStatus.SUCCESS);
			paymentRepository.save(paymentRecord);
		} else {
			log.warn("Payment record not found for razorpayOrderId: {}", razorpayOrderId);
		}
	}

	private PaymentMode mapToPaymentMode(String method) {
		try {
			return PaymentMode.valueOf(method.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.warn("Unknown payment method received: {}", method);
			return null;
		}
	}
}
