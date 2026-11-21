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

	public OrderItemResponseDto createOrderItem(OrderItemRequestDto orderItemRequestDto) {
		Order order = orderRepository.findById(orderItemRequestDto.orderId())
				.orElseThrow(() -> new RuntimeException("Order not found"));
		Product product = productRepository.findById(orderItemRequestDto.productId())
				.orElseThrow(() -> new RuntimeException("Product not found"));

		OrderItem orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setProduct(product);
		orderItem.setQuantity(orderItemRequestDto.quantity());
		orderItem.setPriceAtPurchase(product.getPrice());

		return OrderItemResponseDto.from(orderItemRepository.save(orderItem));
	}

	public List<OrderItemResponseDto> getAllOrderItems() {
		return orderItemRepository.findAll().stream()
				.map(OrderItemResponseDto::from)
				.toList();
	}

	public OrderItemResponseDto getOrderItemById(Long id) {
		return orderItemRepository.findById(id)
				.map(OrderItemResponseDto::from)
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

		return OrderItemResponseDto.from(orderItemRepository.save(existingOrderItem));
	}

	public void deleteOrderItemById(Long id) {
		if (!orderItemRepository.existsById(id)) {
			throw new RuntimeException("Order item not found");
		}
		orderItemRepository.deleteById(id);
	}
}
