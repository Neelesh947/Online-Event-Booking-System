package in.online.event.booking.event.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAvailabilityResponse {

	private String eventId;
    private String title;
    private String location;
    private String category;
    private int totalTickets;
    private int availableTickets;
    private double ticketPrice;
    private String organizerId;
}
