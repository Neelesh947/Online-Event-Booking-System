package in.online.event.booking.event.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import in.online.event.booking.event.dto.CreateOrderRequestDto;
import in.online.event.booking.event.dto.CreateOrderResponseDto;
import jakarta.validation.Valid;

public interface PaymentService {

	public CreateOrderResponseDto createPaymentOrder(@Valid CreateOrderRequestDto request);
	
	public ResponseEntity<?> verifyPayment(Map<String, String> paymentData);

}
