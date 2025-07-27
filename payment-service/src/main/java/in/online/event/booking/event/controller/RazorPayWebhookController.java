package in.online.event.booking.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.online.event.booking.event.service.WebhookService;
import in.online.event.booking.event.utils.Constants;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/{realm}/webhook")
@RequiredArgsConstructor
public class RazorPayWebhookController {

	private final WebhookService webhookService;

	@PostMapping("/call-back")
	public ResponseEntity<String> handleWebhook(@RequestBody String payload,
			@RequestHeader(Constants.RAZORPAY_SIGNATURE) String razorpaySignature) {

		webhookService.processWebhook(payload, razorpaySignature);
		return ResponseEntity.ok().build();
	}
}
