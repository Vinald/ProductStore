package online.vinald.productstore.orderItem;

import online.vinald.productstore.exception.InsufficientStockException;
import online.vinald.productstore.order.Order;
import online.vinald.productstore.order.OrderRepository;
import online.vinald.productstore.product.Product;
import online.vinald.productstore.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	private OrderItemService orderItemService;

	@BeforeEach
	void setUp() {
		orderItemService = new OrderItemService(orderItemRepository, orderRepository, productRepository, new OrderItemMapper());
	}

	private Product productWithStock(long id, BigDecimal price, int quantity) {
		Product product = new Product();
		product.setId(id);
		product.setName("Widget");
		product.setPrice(price);
		product.setQuantity(quantity);
		return product;
	}

	private Order order(long id) {
		Order order = new Order();
		order.setId(id);
		order.setTotalAmount(BigDecimal.ZERO);
		return order;
	}

	private void stubRecalculation(Order orderEntity, List<OrderItem> itemsAfterChange) {
		when(orderItemRepository.findByOrderId(orderEntity.getId())).thenReturn(itemsAfterChange);
		when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));
		when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
	}

	@Test
	void createOrderItem_reservesStockAndRecalculatesOrderTotal() {
		Order orderEntity = order(1L);
		Product product = productWithStock(10L, new BigDecimal("10.00"), 5);
		when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
		when(productRepository.findById(10L)).thenReturn(Optional.of(product));
		when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
			OrderItem item = invocation.getArgument(0);
			item.setId(100L);
			return item;
		});

		OrderItemRequestDto request = new OrderItemRequestDto(1L, 10L, 2);
		stubRecalculation(orderEntity, List.of(itemFor(product, 2)));

		OrderItemResponseDto response = orderItemService.createOrderItem(request);

		assertThat(response.priceAtPurchase()).isEqualByComparingTo("10.00");
		assertThat(product.getQuantity()).isEqualTo(3);
		assertThat(orderEntity.getTotalAmount()).isEqualByComparingTo("20.00");
	}

	@Test
	void createOrderItem_insufficientStock_throwsAndDoesNotSave() {
		Order orderEntity = order(1L);
		Product product = productWithStock(10L, new BigDecimal("10.00"), 1);
		when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
		when(productRepository.findById(10L)).thenReturn(Optional.of(product));

		OrderItemRequestDto request = new OrderItemRequestDto(1L, 10L, 5);

		assertThatThrownBy(() -> orderItemService.createOrderItem(request))
				.isInstanceOf(InsufficientStockException.class);
	}

	private OrderItem itemFor(Product product, int quantity) {
		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setQuantity(quantity);
		item.setPriceAtPurchase(product.getPrice());
		return item;
	}

	@Test
	void updateOrderItem_quantityIncrease_reservesTheDelta() {
		Order orderEntity = order(1L);
		Product product = productWithStock(10L, new BigDecimal("10.00"), 5);

		OrderItem existing = new OrderItem();
		existing.setId(100L);
		existing.setOrder(orderEntity);
		existing.setProduct(product);
		existing.setQuantity(2);
		existing.setPriceAtPurchase(product.getPrice());

		when(orderItemRepository.findById(100L)).thenReturn(Optional.of(existing));
		when(orderItemRepository.save(existing)).thenReturn(existing);
		stubRecalculation(orderEntity, List.of(existing));

		OrderItemRequestDto request = new OrderItemRequestDto(1L, 10L, 4);

		orderItemService.updateOrderItem(100L, request);

		assertThat(product.getQuantity()).isEqualTo(3);
		assertThat(existing.getQuantity()).isEqualTo(4);
	}

	@Test
	void updateOrderItem_quantityDecrease_releasesTheDelta() {
		Order orderEntity = order(1L);
		Product product = productWithStock(10L, new BigDecimal("10.00"), 5);

		OrderItem existing = new OrderItem();
		existing.setId(100L);
		existing.setOrder(orderEntity);
		existing.setProduct(product);
		existing.setQuantity(4);
		existing.setPriceAtPurchase(product.getPrice());

		when(orderItemRepository.findById(100L)).thenReturn(Optional.of(existing));
		when(orderItemRepository.save(existing)).thenReturn(existing);
		stubRecalculation(orderEntity, List.of(existing));

		OrderItemRequestDto request = new OrderItemRequestDto(1L, 10L, 1);

		orderItemService.updateOrderItem(100L, request);

		assertThat(product.getQuantity()).isEqualTo(8);
		assertThat(existing.getQuantity()).isEqualTo(1);
	}

	@Test
	void deleteOrderItemById_releasesStockAndRecalculatesTotal() {
		Order orderEntity = order(1L);
		Product product = productWithStock(10L, new BigDecimal("10.00"), 3);

		OrderItem existing = new OrderItem();
		existing.setId(100L);
		existing.setOrder(orderEntity);
		existing.setProduct(product);
		existing.setQuantity(2);
		existing.setPriceAtPurchase(product.getPrice());

		when(orderItemRepository.findById(100L)).thenReturn(Optional.of(existing));
		stubRecalculation(orderEntity, List.of());

		orderItemService.deleteOrderItemById(100L);

		assertThat(product.getQuantity()).isEqualTo(5);
		assertThat(orderEntity.getTotalAmount()).isEqualByComparingTo("0.00");
	}
}
