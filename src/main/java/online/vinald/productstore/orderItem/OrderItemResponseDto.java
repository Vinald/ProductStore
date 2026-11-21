package online.vinald.productstore.orderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderItemResponseDto(
		Long id,
		Integer quantity,
		BigDecimal priceAtPurchase,
		LocalDate createdAt,
		LocalDate updatedAt,
		UUID uuid,
		Long orderId,
		Long productId
) {
}
