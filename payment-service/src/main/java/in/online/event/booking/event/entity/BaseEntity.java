package in.online.event.booking.event.entity;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

	public static final String ENTITY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	@Id
	@Column(updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();

	@Column(name = "create_date_time", updatable = false)
	@CreationTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ENTITY_DATE_FORMAT)
	private Timestamp createDateTime;

	@Column(name = "update_date_time")
	@UpdateTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ENTITY_DATE_FORMAT)
	private Timestamp updateDateTime;
}
