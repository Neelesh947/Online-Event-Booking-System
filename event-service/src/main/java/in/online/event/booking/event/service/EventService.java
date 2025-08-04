package in.online.event.booking.event.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import in.online.event.booking.event.dto.EventRequest;
import in.online.event.booking.event.dto.EventResponse;
import jakarta.validation.Valid;

public interface EventService {

	Page<EventResponse> getAllEvents(String realm, Optional<String> category, Optional<String> location,
			Optional<String> from, Optional<String> to, Optional<String> keyword, Pageable pageable);

	EventResponse getEventById(String realm, String eventId);

	EventResponse createEvent(String realm, @Valid EventRequest eventRequest, String createdBy);

	EventResponse updateEvent(String realm, String eventId, @Valid EventRequest request, String updateBy);

	public void deleteEvent(String realm, String eventId);

	public void reduceTickets(String realm, String eventId, int count);

	public void increaseTickets(String realm, String eventId, int count);

	List<EventResponse> getEventByOrganizerId(String organizerId, String realm);
}
