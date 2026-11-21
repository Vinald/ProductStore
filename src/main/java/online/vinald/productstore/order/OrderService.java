package online.vinald.productstore.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;

	public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
		Order order = orderMapper.toEntity(orderRequestDto);
		return orderMapper.toResponseDto(orderRepository.save(order));
	}

	public List<OrderResponseDto> getAllOrders() {
		return orderRepository.findAll().stream()
				.map(orderMapper::toResponseDto)
				.toList();
	}

	public OrderResponseDto getOrderById(Long id) {
		return orderRepository.findById(id)
				.map(orderMapper::toResponseDto)
				.orElseThrow(() -> new RuntimeException("Order not found"));
	}

	public OrderResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto) {
		Order existingOrder = orderRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Order not found"));

		orderMapper.updateEntity(existingOrder, orderRequestDto);

		return orderMapper.toResponseDto(orderRepository.save(existingOrder));
	}

	public void deleteOrderById(Long id) {
		if (!orderRepository.existsById(id)) {
			throw new RuntimeException("Order not found");
		}
		orderRepository.deleteById(id);
	}
}
