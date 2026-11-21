package online.vinald.productstore.order;

import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

	public Order toEntity(OrderCreateRequestDto orderCreateRequestDto) {
		Order order = new Order();
		order.setCustomerName(orderCreateRequestDto.customerName());
		order.setCustomerEmail(orderCreateRequestDto.customerEmail());
		order.setStatus(orderCreateRequestDto.status());
		return order;
	}

	public void updateEntity(Order order, OrderRequestDto orderRequestDto) {
		order.setCustomerName(orderRequestDto.customerName());
		order.setCustomerEmail(orderRequestDto.customerEmail());
		order.setStatus(orderRequestDto.status());
	}

	public OrderResponseDto toResponseDto(Order order) {
		return new OrderResponseDto(
				order.getId(),
				order.getUser().getId(),
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
