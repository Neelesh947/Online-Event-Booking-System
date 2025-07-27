package in.online.event.booking.event.service;

import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import in.online.event.booking.event.dto.CreateOrderRequestDto;
import in.online.event.booking.event.dto.CreateOrderResponseDto;
import in.online.event.booking.event.entity.Payment;
import in.online.event.booking.event.enums.PaymentGateway;
import in.online.event.booking.event.enums.PaymentStatus;
import in.online.event.booking.event.repository.PaymentRepository;
import in.online.event.booking.event.utils.Constants;
import in.online.event.booking.event.utils.OrderPayload;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	@Value("${razorpay.key-id}")
	private String razorpayKey;

	@Value("${razorpay.key-secret}")
	private String razorpaySecret;

	private RazorpayClient razorpayClient;

	private final PaymentRepository paymentRepository;

	@PostConstruct
	public void init() throws Exception {
		razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
	}

	@Override
	public CreateOrderResponseDto createPaymentOrder(@Valid CreateOrderRequestDto request) {
		try {
			JSONObject orderPayload = OrderPayload.buildPayloadForCreateOrder(request.getAmountInPaise(), Constants.INR,
					Constants.RECEIPT_ID + System.currentTimeMillis(), 1);

			Order order = razorpayClient.orders.create(orderPayload);

			Payment record = Payment.builder().paymentGateway(PaymentGateway.RAZORPAY)
					.amountInPaise(request.getAmountInPaise()).eventId(request.getEventId())
					.userId(request.getCustomerId()).paymentStatus(PaymentStatus.CREATED)
					.paymentReferenceUrl(order.get("id")).build();

			paymentRepository.save(record);

			return new CreateOrderResponseDto(order.get("id"), razorpayKey, order.get("amount"), order.get("currency"));
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
		}
	}

	public ResponseEntity<?> verifyPayment(Map<String, String> paymentData) {
		try {
			String razorpayOrderId = paymentData.get("razorpay_order_id");
			String razorpayPaymentId = paymentData.get("razorpay_payment_id");
			String razorpaySignature = paymentData.get("razorpay_signature");

			String secret = razorpaySecret;

			try {
				String data = razorpayOrderId + "|" + razorpayPaymentId;
				String generatedSignature = hmacSha256(data, secret);

				if (generatedSignature.equals(razorpaySignature)) {
					return ResponseEntity.ok(Map.of("success", true));
				} else {
					return ResponseEntity.ok(Map.of("success", false));
				}

			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Internal server error"));
		}
	}

	private String hmacSha256(String data, String key) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
	}
}
