package in.online.event.booking.event.service;

public interface WebhookService {

	void processWebhook(String payload, String razorpaySignature);
}
