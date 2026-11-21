package online.vinald.productstore.orderItem;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.order.Order;
import online.vinald.productstore.order.OrderRepository;
import online.vinald.productstore.product.Product;
import online.vinald.productstore.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
	private final OrderItemRepository orderItemRepository;
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final OrderItemMapper orderItemMapper;

	public OrderItemResponseDto createOrderItem(OrderItemRequestDto orderItemRequestDto) {
		Order order = orderRepository.findById(orderItemRequestDto.orderId())
				.orElseThrow(() -> new RuntimeException("Order not found"));
		Product product = productRepository.findById(orderItemRequestDto.productId())
				.orElseThrow(() -> new RuntimeException("Product not found"));

		OrderItem orderItem = orderItemMapper.toEntity(orderItemRequestDto, order, product);

		return orderItemMapper.toResponseDto(orderItemRepository.save(orderItem));
	}

	public List<OrderItemResponseDto> getAllOrderItems() {
		return orderItemRepository.findAll().stream()
				.map(orderItemMapper::toResponseDto)
				.toList();
	}

	public OrderItemResponseDto getOrderItemById(Long id) {
		return orderItemRepository.findById(id)
				.map(orderItemMapper::toResponseDto)
				.orElseThrow(() -> new RuntimeException("Order item not found"));
	}

	public OrderItemResponseDto updateOrderItem(Long id, OrderItemRequestDto orderItemRequestDto) {
		OrderItem existingOrderItem = orderItemRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Order item not found"));

		if (!existingOrderItem.getOrder().getId().equals(orderItemRequestDto.orderId())) {
			Order order = orderRepository.findById(orderItemRequestDto.orderId())
					.orElseThrow(() -> new RuntimeException("Order not found"));
			existingOrderItem.setOrder(order);
		}

		if (!existingOrderItem.getProduct().getId().equals(orderItemRequestDto.productId())) {
			Product product = productRepository.findById(orderItemRequestDto.productId())
					.orElseThrow(() -> new RuntimeException("Product not found"));
			existingOrderItem.setProduct(product);
			existingOrderItem.setPriceAtPurchase(product.getPrice());
		}

		existingOrderItem.setQuantity(orderItemRequestDto.quantity());

		return orderItemMapper.toResponseDto(orderItemRepository.save(existingOrderItem));
	}

	public void deleteOrderItemById(Long id) {
		if (!orderItemRepository.existsById(id)) {
			throw new RuntimeException("Order item not found");
		}
		orderItemRepository.deleteById(id);
	}
}
