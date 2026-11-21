package online.vinald.productstore.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderResponseDto(
		Long id,
		String customerName,
		String customerEmail,
		String status,
		BigDecimal totalAmount,
		LocalDate createdAt,
		LocalDate updatedAt,
		UUID uuid
) {
	public static OrderResponseDto from(Order order) {
		return new OrderResponseDto(
				order.getId(),
				order.getCustomerName(),
				order.getCustomerEmail(),
				order.getStatus(),
				order.getTotalAmount(),
				order.getCreatedAt(),
				order.getUpdatedAt(),
				order.getUuid()
		);
	}
}
