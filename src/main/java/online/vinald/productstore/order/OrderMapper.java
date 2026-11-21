package online.vinald.productstore.order;

import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

	public Order toEntity(OrderRequestDto orderRequestDto) {
		Order order = new Order();
		updateEntity(order, orderRequestDto);
		return order;
	}

	public void updateEntity(Order order, OrderRequestDto orderRequestDto) {
		order.setCustomerName(orderRequestDto.customerName());
		order.setCustomerEmail(orderRequestDto.customerEmail());
		order.setStatus(orderRequestDto.status());
		order.setTotalAmount(orderRequestDto.totalAmount());
	}

	public OrderResponseDto toResponseDto(Order order) {
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
