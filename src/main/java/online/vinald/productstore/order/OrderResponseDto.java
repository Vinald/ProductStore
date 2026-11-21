package online.vinald.productstore.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderResponseDto(
		Long id,
		Long userId,
		String customerName,
		String customerEmail,
		String status,
		BigDecimal totalAmount,
		LocalDate createdAt,
		LocalDate updatedAt,
		UUID uuid
) {
}
