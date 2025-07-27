package in.online.event.booking.event.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

	@NotBlank(message = "Title is required")
	private String title;

	@Lob
	@NotBlank(message = "Description is required")
	private String description;

	@NotBlank(message = "Location is required")
	private String location;

	@Future(message = "Start time must be in the future")
	private LocalDateTime startTime;

	@Future(message = "End time must be in the future")
	private LocalDateTime endTime;

	@Min(value = 1, message = "Total tickets must be at least 1")
	private int totalTickets;

	@Min(value = 0, message = "Available tickets can't be negative")
	private int availableTickets;

	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
	private double ticketPrice;

	private String imageUrl;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@NotBlank(message = "Organizer ID is required")
	private String organizerId;

}
