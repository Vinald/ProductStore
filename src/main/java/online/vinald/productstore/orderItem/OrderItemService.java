package online.vinald.productstore.orderItem;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.exception.InsufficientStockException;
import online.vinald.productstore.exception.ResourceNotFoundException;
import online.vinald.productstore.order.Order;
import online.vinald.productstore.order.OrderRepository;
import online.vinald.productstore.product.Product;
import online.vinald.productstore.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {
	private final OrderItemRepository orderItemRepository;
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final OrderItemMapper orderItemMapper;

	public OrderItemResponseDto createOrderItem(OrderItemRequestDto orderItemRequestDto) {
		Order order = orderRepository.findById(orderItemRequestDto.orderId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		Product product = productRepository.findById(orderItemRequestDto.productId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		reserveStock(product, orderItemRequestDto.quantity());

		OrderItem orderItem = orderItemMapper.toEntity(orderItemRequestDto, order, product);
		OrderItem savedOrderItem = orderItemRepository.save(orderItem);

		recalculateOrderTotal(order.getId());

		return orderItemMapper.toResponseDto(savedOrderItem);
	}

	@Transactional(readOnly = true)
	public Page<OrderItemResponseDto> getAllOrderItems(Pageable pageable) {
		return orderItemRepository.findAll(pageable)
				.map(orderItemMapper::toResponseDto);
	}

	@Transactional(readOnly = true)
	public OrderItemResponseDto getOrderItemById(Long id) {
		return orderItemRepository.findById(id)
				.map(orderItemMapper::toResponseDto)
				.orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
	}

	public OrderItemResponseDto updateOrderItem(Long id, OrderItemRequestDto orderItemRequestDto) {
		OrderItem existingOrderItem = orderItemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

		Long oldOrderId = existingOrderItem.getOrder().getId();
		boolean orderChanged = !oldOrderId.equals(orderItemRequestDto.orderId());
		boolean productChanged = !existingOrderItem.getProduct().getId().equals(orderItemRequestDto.productId());

		if (orderChanged) {
			Order order = orderRepository.findById(orderItemRequestDto.orderId())
					.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
			existingOrderItem.setOrder(order);
		}

		if (productChanged) {
			releaseStock(existingOrderItem.getProduct(), existingOrderItem.getQuantity());

			Product newProduct = productRepository.findById(orderItemRequestDto.productId())
					.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
			reserveStock(newProduct, orderItemRequestDto.quantity());

			existingOrderItem.setProduct(newProduct);
			existingOrderItem.setPriceAtPurchase(newProduct.getPrice());
		} else {
			int quantityDelta = orderItemRequestDto.quantity() - existingOrderItem.getQuantity();
			if (quantityDelta > 0) {
				reserveStock(existingOrderItem.getProduct(), quantityDelta);
			} else if (quantityDelta < 0) {
				releaseStock(existingOrderItem.getProduct(), -quantityDelta);
			}
		}

		existingOrderItem.setQuantity(orderItemRequestDto.quantity());

		OrderItem savedOrderItem = orderItemRepository.save(existingOrderItem);

		recalculateOrderTotal(savedOrderItem.getOrder().getId());
		if (orderChanged) {
			recalculateOrderTotal(oldOrderId);
		}

		return orderItemMapper.toResponseDto(savedOrderItem);
	}

	public void deleteOrderItemById(Long id) {
		OrderItem orderItem = orderItemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

		releaseStock(orderItem.getProduct(), orderItem.getQuantity());

		Long orderId = orderItem.getOrder().getId();
		orderItemRepository.delete(orderItem);

		recalculateOrderTotal(orderId);
	}

	private void reserveStock(Product product, int quantity) {
		if (product.getQuantity() < quantity) {
			throw new InsufficientStockException("Insufficient stock for product '" + product.getName()
					+ "': requested " + quantity + ", available " + product.getQuantity());
		}
		product.setQuantity(product.getQuantity() - quantity);
		productRepository.save(product);
	}

	private void releaseStock(Product product, int quantity) {
		product.setQuantity(product.getQuantity() + quantity);
		productRepository.save(product);
	}

	private void recalculateOrderTotal(Long orderId) {
		BigDecimal total = orderItemRepository.findByOrderId(orderId).stream()
				.map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		order.setTotalAmount(total);
		orderRepository.save(order);
	}
}
