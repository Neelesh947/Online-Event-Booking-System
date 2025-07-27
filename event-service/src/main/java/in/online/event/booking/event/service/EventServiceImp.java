package in.online.event.booking.event.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import in.online.event.booking.event.dto.EventMapper;
import in.online.event.booking.event.dto.EventRequest;
import in.online.event.booking.event.dto.EventResponse;
import in.online.event.booking.event.entity.Category;
import in.online.event.booking.event.entity.Event;
import in.online.event.booking.event.repository.CategoryRepository;
import in.online.event.booking.event.repository.EventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImp implements EventService {

	private final EventRepository eventRepository;
	private final EventMapper eventMapper;
	private final CategoryRepository categoryRepository;

	@Override
	public Page<EventResponse> getAllEvents(String realm, Optional<String> category, Optional<String> location,
			Optional<String> from, Optional<String> to, Optional<String> keyword, Pageable pageable) {
		LocalDateTime fromTime = from.filter(f -> !f.isBlank()).map(f -> LocalDateTime.parse(f + "T00:00:00"))
				.orElse(null);
		LocalDateTime toTime = to.filter(t -> !t.isBlank()).map(t -> LocalDateTime.parse(t + "T23:59:59")).orElse(null);

		String search = keyword.filter(k -> !k.isBlank()).orElse(null);
		String categoryId = category.filter(c -> !c.isBlank()).orElse(null);

		List<Event> filteredEvents = eventRepository.filterEvents(search, categoryId, fromTime, toTime);

		if (location.isPresent() && !location.get().isBlank()) {
			String loc = location.get().toLowerCase();
			filteredEvents = filteredEvents.stream()
					.filter(e -> e.getLocation() != null && e.getLocation().toLowerCase().contains(loc)).toList();
		}

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filteredEvents.size());
		List<EventResponse> eventResponses = filteredEvents.subList(start, end).stream().map(eventMapper::toResponse)
				.toList();
		return new PageImpl<>(eventResponses, pageable, filteredEvents.size());
	}

	@Override
	public EventResponse getEventById(String realm, String eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Event Not Found"));
		return eventMapper.toResponse(event);
	}

	@Override
	public EventResponse createEvent(String realm, @Valid EventRequest eventRequest, String createdBy) {
		Category category = categoryRepository.findById(eventRequest.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		Event event = Event.builder().title(eventRequest.getTitle()).description(eventRequest.getDescription())
				.location(eventRequest.getLocation()).startTime(eventRequest.getStartTime())
				.endTime(eventRequest.getEndTime()).totalTickets(eventRequest.getTotalTickets())
				.availableTickets(eventRequest.getTotalTickets()).ticketPrice(eventRequest.getTicketPrice())
				.imageUrl(eventRequest.getImageUrl()).category(category).organizerId(createdBy).build();
		Event savedEvent = eventRepository.save(event);
		return eventMapper.toResponse(savedEvent);
	}

	@Override
	public EventResponse updateEvent(String realm, String eventId, @Valid EventRequest request, String updateBy) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Event not found"));

		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		event.setTitle(request.getTitle());
		event.setDescription(request.getDescription());
		event.setLocation(request.getLocation());
		event.setStartTime(request.getStartTime());
		event.setEndTime(request.getEndTime());
		event.setTotalTickets(request.getTotalTickets());

		int currentAvailable = event.getAvailableTickets();
		int newTotal = request.getTotalTickets();
		if (newTotal > event.getTotalTickets()) {
			event.setAvailableTickets(currentAvailable + (newTotal - event.getTotalTickets()));
		}

		event.setTicketPrice(request.getTicketPrice());
		event.setImageUrl(request.getImageUrl());
		event.setCategory(category);

		event.setOrganizerId(updateBy);
		Event updatedEvent = eventRepository.save(event);
		return eventMapper.toResponse(updatedEvent);
	}

	@Override
	public void deleteEvent(String realm, String eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: {}", eventId));
		eventRepository.delete(event);
	}

	@Override
	public void reduceTickets(String realm, String eventId, int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Ticket count must be greater than zero.");
		}
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

		if (event.getAvailableTickets() < count) {
			throw new ResourceNotFoundException("Insufficient available tickets for event ID: " + eventId);
		}

		event.setAvailableTickets(event.getAvailableTickets() - count);
		eventRepository.save(event);
	}

	public void increaseTickets(String realm, String eventId, int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Ticket count must be greater than zero.");
		}
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));
		event.setAvailableTickets(event.getAvailableTickets() + count);
		eventRepository.save(event);
	}

}
