package in.online.event.booking.event.utils;

import org.json.JSONObject;

public class OrderPayload {

	/**
	 * Builds the Razorpay order creation payload.
	 *
	 * @param amountInPaise  Amount in paise (e.g., ₹100 = 10000)
	 * @param currency       Currency code (e.g., INR)
	 * @param receiptId      Unique receipt ID (optional, used for tracking)
	 * @param paymentCapture Whether to auto-capture the payment (1 = yes, 0 = no)
	 * @return JSONObject for Razorpay order creation API
	 */
	public static JSONObject buildPayloadForCreateOrder(int amountInPaise, String currency, String receiptId,
			int paymentCapture) {
		JSONObject payload = new JSONObject();
		payload.put(Constants.AMOUNT, amountInPaise);
		payload.put(Constants.CURRENCY, currency != null ? currency : Constants.INR);
		payload.put(Constants.RECEIPT,
				receiptId != null ? receiptId : Constants.RECEIPT_ID + System.currentTimeMillis());
		payload.put(Constants.PAYMENT_CAPTURE, paymentCapture); // 1 = auto capture
		return payload;
	}
}
