package in.online.event.booking.event.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.online.event.booking.event.dto.CreateOrderRequestDto;
import in.online.event.booking.event.dto.CreateOrderResponseDto;
import in.online.event.booking.event.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/{realm}/api/v1/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/create")
	public ResponseEntity<CreateOrderResponseDto> createOrder(@RequestBody @Valid CreateOrderRequestDto request) {
		return ResponseEntity.ok(paymentService.createPaymentOrder(request));
	}

	@PostMapping("/verify")
	public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> paymentData) {
		return paymentService.verifyPayment(paymentData);
	}
}
