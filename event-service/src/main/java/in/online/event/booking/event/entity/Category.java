package in.online.event.booking.event.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity{

	@Column(unique = true, nullable = false)
    @NotBlank(message = "Category name is required")
    private String name;
}
