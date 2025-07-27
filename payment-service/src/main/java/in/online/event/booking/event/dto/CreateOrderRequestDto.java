package in.online.event.booking.event.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderRequestDto {

	private String customerId;
	private String eventId;
	private Integer amountInPaise;
}
