package in.online.event.booking.event.service;

import java.util.List;

import in.online.event.booking.event.dto.BookingRequestDto;
import in.online.event.booking.event.dto.BookingResponseDto;

public interface BookingService {

	BookingResponseDto createBooking(String realm, String userId, BookingRequestDto bookingRequest);

    BookingResponseDto cancelBooking(String realm, String bookingId, String userId);

    BookingResponseDto getBookingById(String realm, String bookingId);

    List<BookingResponseDto> getUserBookings(String realm, String userId);
}
