package online.vinald.productstore.orderItem;

import online.vinald.productstore.order.Order;
import online.vinald.productstore.product.Product;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

	public OrderItem toEntity(OrderItemRequestDto orderItemRequestDto, Order order, Product product) {
		OrderItem orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setProduct(product);
		orderItem.setQuantity(orderItemRequestDto.quantity());
		orderItem.setPriceAtPurchase(product.getPrice());
		return orderItem;
	}

	public OrderItemResponseDto toResponseDto(OrderItem orderItem) {
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
