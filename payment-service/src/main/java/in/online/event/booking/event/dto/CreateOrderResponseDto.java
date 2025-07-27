package in.online.event.booking.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponseDto {
    private String razorpayOrderId;
    private String key;
    private Integer amountInPaise;
    private String currency;
}
