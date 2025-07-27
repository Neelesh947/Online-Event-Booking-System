package in.online.event.booking.event.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.online.event.booking.event.dto.EventRequest;
import in.online.event.booking.event.dto.EventResponse;
import in.online.event.booking.event.service.EventService;
import in.online.event.booking.event.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{realm}/events")
public class EventController {

	private final EventService eventService;

	/*
	 * Public - Get all events with optional filters + pagination
	 */

	@GetMapping
	public ResponseEntity<Page<EventResponse>> getAllEvents(@PathVariable String realm,
			@RequestParam Optional<String> category, @RequestParam Optional<String> location,
			@RequestParam Optional<String> from, @RequestParam Optional<String> to,
			@RequestParam Optional<String> keyword, Pageable pageable) {

		Page<EventResponse> events = eventService.getAllEvents(realm, category, location, from, to, keyword, pageable);
		return ResponseEntity.ok(events);
	}

	/**
	 * Public - Get event details by ID
	 * 
	 * @param realm
	 * @param eventId
	 * @return
	 */
	@GetMapping("/{eventId}")
	public ResponseEntity<EventResponse> getEventById(@PathVariable String realm, @PathVariable String eventId) {
		EventResponse event = eventService.getEventById(realm, eventId);
		return ResponseEntity.ok(event);
	}

	/**
	 * Organizer - Create event
	 * 
	 * @param realm
	 * @param eventRequest
	 * @return
	 */
	@PostMapping
	@PreAuthorize("hasRole('ORGANIZER')")
	public ResponseEntity<EventResponse> createEvent(@PathVariable String realm,
			@Valid @RequestBody EventRequest eventRequest) {
		String createdBy = SecurityUtils.getCurrentUserIdSupplier.get();
		EventResponse event = eventService.createEvent(realm, eventRequest, createdBy);
		return ResponseEntity.status(HttpStatus.CREATED).body(event);
	}

	/**
	 * Organizer, super_admin - Update event
	 * 
	 * @param realm
	 * @param eventId
	 * @param request
	 * @return
	 */
	@PutMapping("/{eventId}")
	@PreAuthorize("hasRole('ORGANIZER', 'SUPER_ADMIN')")
	public ResponseEntity<EventResponse> updateEvent(@PathVariable String realm, @PathVariable String eventId,
			@Valid @RequestBody EventRequest request) {
		String updateBy = SecurityUtils.getCurrentUserIdSupplier.get();
		EventResponse updated = eventService.updateEvent(realm, eventId, request, updateBy);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Organizer, super_admin - Delete event
	 * 
	 * @param realm
	 * @param eventId
	 * @return
	 */
	@DeleteMapping("/{eventId}")
	@PreAuthorize("hasRole('ORGANIZER', 'SUPER_ADMIN')")
	public ResponseEntity<Void> deleteEvent(@PathVariable String realm, @PathVariable String eventId) {
		eventService.deleteEvent(realm, eventId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Reduce available tickets for a given event.
	 *
	 * @param realm   The Keycloak realm.
	 * @param eventId The event ID.
	 * @param count   Number of tickets to reduce.
	 * @return HTTP 200 if successful.
	 */
	@PutMapping("/{eventId}/reduce-tickets")
	public ResponseEntity<Void> reduceTickets(@PathVariable String realm, @PathVariable String eventId,
			@RequestParam("count") int count) {
		eventService.reduceTickets(realm, eventId, count);
		return ResponseEntity.ok().build();
	}

	/**
	 * Increase available tickets for a given event.
	 *
	 * @param realm   The Keycloak realm.
	 * @param eventId The event ID.
	 * @param count   Number of tickets to increase.
	 * @return HTTP 200 if successful.
	 */
	@PutMapping("/{eventId}/increase-tickets")
	public ResponseEntity<Void> increaseTickets(@PathVariable String realm, @PathVariable String eventId,
			@RequestParam("count") int count) {
		eventService.increaseTickets(realm, eventId, count);
		return ResponseEntity.ok().build();
	}
}
