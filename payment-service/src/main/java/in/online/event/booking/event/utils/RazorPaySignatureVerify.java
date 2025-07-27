package in.online.event.booking.event.utils;

import org.springframework.stereotype.Component;

import com.razorpay.Utils;

@Component
public class RazorPaySignatureVerify {

	/**
     * Verifies the Razorpay webhook signature.
     *
     * @param payload The webhook request body (raw JSON string)
     * @param razorpaySignature The value of the "X-Razorpay-Signature" header
     * @param webhookSecret The secret key set in the Razorpay dashboard
     * @return true if signature is valid, false otherwise
     */
	public static boolean isSignatureValid(String payload, String razorpaySignature, String webhookSecret) {
		try {
			return Utils.verifyWebhookSignature(payload, razorpaySignature, webhookSecret);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
