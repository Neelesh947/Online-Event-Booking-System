package in.online.event.booking.event.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import in.online.event.booking.event.dto.BookingMapper;
import in.online.event.booking.event.dto.BookingRequestDto;
import in.online.event.booking.event.dto.BookingResponseDto;
import in.online.event.booking.event.entity.Booking;
import in.online.event.booking.event.entity.EventAvailabilityResponse;
import in.online.event.booking.event.enums.BookingStatus;
import in.online.event.booking.event.repository.BookingRepository;
import in.online.event.booking.event.utils.EventServiceUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepository;
	private final EventServiceUtils eventServiceUtils;
	private final BookingMapper bookingMapper;

	@Override
	public BookingResponseDto createBooking(String realm, String userId, BookingRequestDto bookingRequest) {

		EventAvailabilityResponse response = eventServiceUtils.getEventDetails(realm, bookingRequest.getEventId());

		if (response == null) {
			throw new RuntimeException("Event not found for ID: " + bookingRequest.getEventId());
		}
		if (response.getAvailableTickets() < bookingRequest.getTicketCount()) {
			throw new RuntimeException(
					"Not enough tickets available. Only " + response.getAvailableTickets() + " left.");
		}

		Booking booking = Booking.builder().eventId(bookingRequest.getEventId()).userId(userId)
				.ticketCount(bookingRequest.getTicketCount())
				.totalPrice(response.getTicketPrice() * bookingRequest.getTicketCount()).status(BookingStatus.CONFIRMED)
				.bookingDate(LocalDateTime.now()).build();

		Booking saved = bookingRepository.save(booking);
		eventServiceUtils.reduceTickets(realm, bookingRequest.getEventId(), bookingRequest.getTicketCount());

		return bookingMapper.toResponse(saved);
	}

	@Override
	public BookingResponseDto cancelBooking(String realm, String bookingId, String userId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

		if (!booking.getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized to cancel this booking.");
		}

		if (booking.getStatus() == BookingStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already cancelled.");
		}

		booking.setStatus(BookingStatus.CANCELLED);
		bookingRepository.save(booking);
		eventServiceUtils.increaseTickets(realm, booking.getEventId(), booking.getTicketCount());

		return bookingMapper.toResponse(booking);
	}

	@Override
	public BookingResponseDto getBookingById(String realm, String bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
		return bookingMapper.toResponse(booking);
	}

	@Override
	public List<BookingResponseDto> getUserBookings(String realm, String userId) {
		List<Booking> bookings = bookingRepository.findByUserId(userId);
		return bookings.stream().map(bookingMapper::toResponse).collect(Collectors.toList());
	}

}
