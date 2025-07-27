package in.online.event.booking.event.dto;

import org.springframework.stereotype.Component;

import in.online.event.booking.event.entity.Booking;

@Component
public class BookingMapper {

	public BookingResponseDto toResponse(Booking booking) {
		return BookingResponseDto.builder().bookingId(booking.getId()).eventId(booking.getEventId())
				.userId(booking.getUserId()).ticketCount(booking.getTicketCount()).status(booking.getStatus())
				.bookingDate(booking.getBookingDate()).build();
	}

}
