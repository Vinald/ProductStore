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
	public static OrderItemResponseDto from(OrderItem orderItem) {
		return new OrderItemResponseDto(
				orderItem.getId(),
				orderItem.getQuantity(),
				orderItem.getPriceAtPurchase(),
				orderItem.getCreatedAt(),
				orderItem.getUpdatedAt(),
				orderItem.getUuid(),
				orderItem.getOrder() != null ? orderItem.getOrder().getId() : null,
				orderItem.getProduct() != null ? orderItem.getProduct().getId() : null
		);
	}
}
