package in.online.event.booking.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingRequestDto {

	@NotBlank(message = "Event ID is required")
	private String eventId;

	@Min(value = 1, message = "At least 1 ticket must be booked")
	private int ticketCount;
}
