package online.vinald.productstore.order;

import online.vinald.productstore.exception.InsufficientStockException;
import online.vinald.productstore.exception.ResourceNotFoundException;
import online.vinald.productstore.orderItem.OrderItem;
import online.vinald.productstore.orderItem.OrderItemCreateDto;
import online.vinald.productstore.product.Product;
import online.vinald.productstore.product.ProductRepository;
import online.vinald.productstore.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	private OrderService orderService;

	@BeforeEach
	void setUp() {
		orderService = new OrderService(orderRepository, productRepository, new OrderMapper());
	}

	private User userWithRole(long id, String role) {
		User user = new User();
		user.setId(id);
		user.setRole(role);
		return user;
	}

	private Product productWithStock(long id, String name, BigDecimal price, int quantity) {
		Product product = new Product();
		product.setId(id);
		product.setName(name);
		product.setPrice(price);
		product.setQuantity(quantity);
		return product;
	}

	private Order orderOwnedBy(long orderId, User owner) {
		Order order = new Order();
		order.setId(orderId);
		order.setUser(owner);
		order.setOrderItems(List.of());
		return order;
	}

	@Test
	void createOrder_reservesStockAndComputesTotalFromCurrentPrice() {
		User buyer = userWithRole(1L, "USER");
		Product product = productWithStock(10L, "Widget", new BigDecimal("10.00"), 5);
		when(productRepository.findById(10L)).thenReturn(Optional.of(product));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderCreateRequestDto request = new OrderCreateRequestDto(
				"Buyer", "buyer@example.com", "PENDING", List.of(new OrderItemCreateDto(10L, 2)));

		OrderResponseDto response = orderService.createOrder(request, buyer);

		assertThat(response.totalAmount()).isEqualByComparingTo("20.00");
		assertThat(response.userId()).isEqualTo(1L);
		assertThat(product.getQuantity()).isEqualTo(3);
		verify(productRepository).save(product);
	}

	@Test
	void createOrder_insufficientStock_throwsAndDoesNotSaveOrder() {
		User buyer = userWithRole(1L, "USER");
		Product product = productWithStock(10L, "Widget", new BigDecimal("10.00"), 1);
		when(productRepository.findById(10L)).thenReturn(Optional.of(product));

		OrderCreateRequestDto request = new OrderCreateRequestDto(
				"Buyer", "buyer@example.com", "PENDING", List.of(new OrderItemCreateDto(10L, 5)));

		assertThatThrownBy(() -> orderService.createOrder(request, buyer))
				.isInstanceOf(InsufficientStockException.class);

		verify(orderRepository, org.mockito.Mockito.never()).save(any());
	}

	@Test
	void getAllOrders_ownerRole_fetchesAllOrders() {
		User owner = userWithRole(1L, "OWNER");
		Pageable pageable = PageRequest.of(0, 10);
		when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(orderOwnedBy(5L, owner))));

		Page<OrderResponseDto> result = orderService.getAllOrders(owner, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(orderRepository).findAll(pageable);
	}

	@Test
	void getAllOrders_regularUser_fetchesOnlyOwnOrders() {
		User buyer = userWithRole(2L, "USER");
		Pageable pageable = PageRequest.of(0, 10);
		when(orderRepository.findByUserId(2L, pageable)).thenReturn(new PageImpl<>(List.of(orderOwnedBy(5L, buyer))));

		Page<OrderResponseDto> result = orderService.getAllOrders(buyer, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(orderRepository).findByUserId(2L, pageable);
		verify(orderRepository, org.mockito.Mockito.never()).findAll(any(Pageable.class));
	}

	@Test
	void getOrderById_ownerRole_canAccessAnyonesOrder() {
		User owner = userWithRole(1L, "OWNER");
		User someoneElse = userWithRole(99L, "USER");
		Order order = orderOwnedBy(5L, someoneElse);
		when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

		OrderResponseDto response = orderService.getOrderById(5L, owner);

		assertThat(response.id()).isEqualTo(5L);
	}

	@Test
	void getOrderById_regularUser_ownOrder_succeeds() {
		User buyer = userWithRole(2L, "USER");
		Order order = orderOwnedBy(5L, buyer);
		when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

		OrderResponseDto response = orderService.getOrderById(5L, buyer);

		assertThat(response.id()).isEqualTo(5L);
	}

	@Test
	void getOrderById_regularUser_someoneElsesOrder_throwsAccessDeniedException() {
		User buyer = userWithRole(2L, "USER");
		User owner = userWithRole(3L, "USER");
		Order order = orderOwnedBy(5L, owner);
		when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.getOrderById(5L, buyer))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void getOrderById_notFound_throwsResourceNotFoundException() {
		when(orderRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.getOrderById(404L, userWithRole(1L, "OWNER")))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deleteOrderById_restocksEachItemsProductBeforeDeleting() {
		User buyer = userWithRole(2L, "USER");
		Order order = orderOwnedBy(5L, buyer);

		Product product = productWithStock(10L, "Widget", new BigDecimal("10.00"), 3);
		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setQuantity(2);
		order.setOrderItems(List.of(item));

		when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

		orderService.deleteOrderById(5L, buyer);

		assertThat(product.getQuantity()).isEqualTo(5);
		verify(productRepository).save(product);
		verify(orderRepository).delete(order);
	}

	@Test
	void deleteOrderById_someoneElsesOrder_throwsAccessDeniedExceptionAndDoesNotDelete() {
		User buyer = userWithRole(2L, "USER");
		User owner = userWithRole(3L, "USER");
		Order order = orderOwnedBy(5L, owner);
		when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.deleteOrderById(5L, buyer))
				.isInstanceOf(AccessDeniedException.class);

		verify(orderRepository, org.mockito.Mockito.never()).delete(any());
	}
}
