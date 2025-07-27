package in.online.event.booking.event.dto;

import java.time.LocalDateTime;

import in.online.event.booking.event.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDto {

	private String bookingId;
	private String eventId;
	private String userId;
	private int ticketCount;
	private BookingStatus status;
	private LocalDateTime bookingDate;
}
