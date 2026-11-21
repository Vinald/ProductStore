package online.vinald.productstore.order;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.exception.InsufficientStockException;
import online.vinald.productstore.exception.ResourceNotFoundException;
import online.vinald.productstore.orderItem.OrderItem;
import online.vinald.productstore.orderItem.OrderItemCreateDto;
import online.vinald.productstore.product.Product;
import online.vinald.productstore.product.ProductRepository;
import online.vinald.productstore.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
	private static final String OWNER_ROLE = "OWNER";

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final OrderMapper orderMapper;

	public OrderResponseDto createOrder(OrderCreateRequestDto orderCreateRequestDto, User currentUser) {
		Order order = orderMapper.toEntity(orderCreateRequestDto);
		order.setUser(currentUser);

		List<OrderItem> orderItems = new ArrayList<>();
		BigDecimal totalAmount = BigDecimal.ZERO;

		for (OrderItemCreateDto itemDto : orderCreateRequestDto.items()) {
			Product product = productRepository.findById(itemDto.productId())
					.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

			if (product.getQuantity() < itemDto.quantity()) {
				throw new InsufficientStockException("Insufficient stock for product '" + product.getName()
						+ "': requested " + itemDto.quantity() + ", available " + product.getQuantity());
			}

			product.setQuantity(product.getQuantity() - itemDto.quantity());
			productRepository.save(product);

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProduct(product);
			orderItem.setQuantity(itemDto.quantity());
			orderItem.setPriceAtPurchase(product.getPrice());
			orderItems.add(orderItem);

			totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity())));
		}

		order.setOrderItems(orderItems);
		order.setTotalAmount(totalAmount);

		return orderMapper.toResponseDto(orderRepository.save(order));
	}

	@Transactional(readOnly = true)
	public Page<OrderResponseDto> getAllOrders(User currentUser, Pageable pageable) {
		Page<Order> orders = isOwner(currentUser)
				? orderRepository.findAll(pageable)
				: orderRepository.findByUserId(currentUser.getId(), pageable);

		return orders.map(orderMapper::toResponseDto);
	}

	@Transactional(readOnly = true)
	public OrderResponseDto getOrderById(Long id, User currentUser) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		checkOwnership(order, currentUser);

		return orderMapper.toResponseDto(order);
	}

	public OrderResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto, User currentUser) {
		Order existingOrder = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		checkOwnership(existingOrder, currentUser);

		orderMapper.updateEntity(existingOrder, orderRequestDto);

		return orderMapper.toResponseDto(orderRepository.save(existingOrder));
	}

	public void deleteOrderById(Long id, User currentUser) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		checkOwnership(order, currentUser);

		if (order.getOrderItems() != null) {
			for (OrderItem orderItem : order.getOrderItems()) {
				Product product = orderItem.getProduct();
				product.setQuantity(product.getQuantity() + orderItem.getQuantity());
				productRepository.save(product);
			}
		}

		orderRepository.delete(order);
	}

	private boolean isOwner(User user) {
		return OWNER_ROLE.equals(user.getRole());
	}

	private void checkOwnership(Order order, User currentUser) {
		boolean isOrderOwner = order.getUser().getId().equals(currentUser.getId());
		if (!isOwner(currentUser) && !isOrderOwner) {
			throw new AccessDeniedException("You do not have access to this order");
		}
	}
}
