package in.online.event.booking.event.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

	@NotBlank(message = "Title is required")
	private String title;

	@NotBlank(message = "Description is required")
	private String description;

	@NotBlank(message = "Location is required")
	private String location;

	@NotNull(message = "Start time is required")
	private LocalDateTime startTime;

	@NotNull(message = "End time is required")
	private LocalDateTime endTime;

	@Min(value = 1, message = "Total tickets must be at least 1")
	private int totalTickets;

	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
	private double ticketPrice;

	private String imageUrl;

	@NotBlank(message = "Category ID is required")
	private String categoryId;

}
