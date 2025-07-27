package in.online.event.booking.event.dto;

import org.springframework.stereotype.Component;

import in.online.event.booking.event.entity.Event;

@Component
public class EventMapper {

	public EventResponse toResponse(Event event) {
		return EventResponse.builder().id(event.getId()).title(event.getTitle()).description(event.getDescription())
				.categoryName(event.getCategory().getName()).location(event.getLocation())
				.startTime(event.getStartTime()).endTime(event.getEndTime()).createdById(event.getOrganizerId())
				.build();
	}
}
