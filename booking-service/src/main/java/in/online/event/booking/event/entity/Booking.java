package in.online.event.booking.event.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

import in.online.event.booking.event.enums.BookingStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking extends BaseEntity {

	@NotBlank(message = "Event ID is required")
	@Column(nullable = false)
	private String eventId;

	@NotBlank(message = "User ID is required")
	@Column(nullable = false)
	private String userId;

	@Min(value = 1, message = "At least 1 ticket must be booked")
	@Column(nullable = false)
	private int ticketCount;
	
	private double totalPrice;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status;

	@Column(nullable = false)
	private LocalDateTime bookingDate;
}
