package in.online.event.booking.event.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

	private String id;
	private String title;
	private String description;
	private String location;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer capactity;
	private String categoryName;
	private String createdBy;
	private String createdById;
}
