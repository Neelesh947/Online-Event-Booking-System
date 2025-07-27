package in.online.event.booking.event.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.online.event.booking.event.entity.EventAvailabilityResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventServiceUtils {

	private final RestTemplate restTemplate;

	private static final String EVENT_SERVICE_BASE_URL = "http://localhost:1236/Event/events";

	/**
	 * Fetches event details from Event Service.
	 * 
	 * @param realm   The Keycloak realm or tenant identifier.
	 * @param eventId The ID of the event.
	 * @return EventAvailabilityResponse containing available tickets.
	 */
	public EventAvailabilityResponse getEventDetails(String realm, String eventId) {
		String url = EVENT_SERVICE_BASE_URL + "/" + eventId;
		ResponseEntity<EventAvailabilityResponse> response = restTemplate.getForEntity(url,
				EventAvailabilityResponse.class, realm);
		return response.getBody();
	}

	/**
	 * Reduce available tickets for an event.
	 * 
	 * @param realm   The Keycloak realm.
	 * @param eventId The event ID.
	 * @param count   Number of tickets to reduce.
	 */
	@Transactional
	public void reduceTickets(String realm, String eventId, int count) {
		String url = EVENT_SERVICE_BASE_URL + "/" + eventId + "/reduce-tickets?count=" + count;
		restTemplate.exchange(url, HttpMethod.PUT, HttpEntity.EMPTY, Void.class, realm);
	}

	/**
	 * Increase available tickets for an event (e.g., when booking is cancelled).
	 * 
	 * @param realm   The Keycloak realm.
	 * @param eventId The event ID.
	 * @param count   Number of tickets to add back.
	 */
	@Transactional
	public void increaseTickets(String realm, String eventId, int count) {
		String url = EVENT_SERVICE_BASE_URL + "/" + eventId + "/increase-tickets?count=" + count;
		restTemplate.exchange(url, HttpMethod.PUT, HttpEntity.EMPTY, Void.class, realm);
	}

	// Inner static class for event response
//	public static class EventAvailabilityResponse {
//		private int availableTickets;
//
//		public int getAvailableTickets() {
//			return availableTickets;
//		}
//
//		public void setAvailableTickets(int availableTickets) {
//			this.availableTickets = availableTickets;
//		}
//	}

}
