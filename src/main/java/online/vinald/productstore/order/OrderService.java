package online.vinald.productstore.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;

	public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
		Order order = new Order();
		order.setCustomerName(orderRequestDto.customerName());
		order.setCustomerEmail(orderRequestDto.customerEmail());
		order.setStatus(orderRequestDto.status());
		order.setTotalAmount(orderRequestDto.totalAmount());
		return OrderResponseDto.from(orderRepository.save(order));
	}

	public List<OrderResponseDto> getAllOrders() {
		return orderRepository.findAll().stream()
				.map(OrderResponseDto::from)
				.toList();
	}

	public OrderResponseDto getOrderById(Long id) {
		return orderRepository.findById(id)
				.map(OrderResponseDto::from)
				.orElseThrow(() -> new RuntimeException("Order not found"));
	}

	public OrderResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto) {
		Order existingOrder = orderRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Order not found"));

		existingOrder.setCustomerName(orderRequestDto.customerName());
		existingOrder.setCustomerEmail(orderRequestDto.customerEmail());
		existingOrder.setStatus(orderRequestDto.status());
		existingOrder.setTotalAmount(orderRequestDto.totalAmount());

		return OrderResponseDto.from(orderRepository.save(existingOrder));
	}

	public void deleteOrderById(Long id) {
		if (!orderRepository.existsById(id)) {
			throw new RuntimeException("Order not found");
		}
		orderRepository.deleteById(id);
	}
}
