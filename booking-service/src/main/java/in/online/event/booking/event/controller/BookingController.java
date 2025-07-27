package in.online.event.booking.event.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.online.event.booking.event.dto.BookingRequestDto;
import in.online.event.booking.event.dto.BookingResponseDto;
import in.online.event.booking.event.service.BookingService;
import in.online.event.booking.event.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{realm}/bookings")
public class BookingController {

	private final BookingService bookingService;

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<BookingResponseDto> createBooking(@PathVariable String realm,
			@Valid @RequestBody BookingRequestDto bookingRequest) {
		String userId = SecurityUtils.getCurrentUserIdSupplier.get();
		BookingResponseDto response = bookingService.createBooking(realm, userId, bookingRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{bookingId}/cancel")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<BookingResponseDto> cancelBooking(@PathVariable String realm,
			@PathVariable String bookingId) {
		String userId = SecurityUtils.getCurrentUserIdSupplier.get();
		BookingResponseDto response = bookingService.cancelBooking(realm, bookingId, userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{bookingId}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable String realm,
			@PathVariable String bookingId) {
//		String userId = SecurityUtils.getCurrentUserIdSupplier.get();
		BookingResponseDto response = bookingService.getBookingById(realm, bookingId);
		// Optional: add check if booking.userId == userId for security
		return ResponseEntity.ok(response);
	}

	@GetMapping("/user")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<List<BookingResponseDto>> getUserBookings(@PathVariable String realm) {
		String userId = SecurityUtils.getCurrentUserIdSupplier.get();
		List<BookingResponseDto> bookings = bookingService.getUserBookings(realm, userId);
		return ResponseEntity.ok(bookings);
	}
}
