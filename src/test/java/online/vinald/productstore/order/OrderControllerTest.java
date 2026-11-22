package online.vinald.productstore.order;

import online.vinald.productstore.TestSecuritySupport;
import online.vinald.productstore.exception.InsufficientStockException;
import online.vinald.productstore.orderItem.OrderItemCreateDto;
import online.vinald.productstore.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderService orderService;

	private OrderResponseDto sampleOrder(Long id, Long userId) {
		return new OrderResponseDto(id, userId, "Buyer", "buyer@example.com", "PENDING",
				new BigDecimal("20.00"), null, null, null);
	}

	@Test
	void createOrder_delegatesToServiceWithAuthenticatedUser() throws Exception {
		User buyer = TestSecuritySupport.userWithRole(1L, "buyer@example.com", "USER");
		when(orderService.createOrder(any(OrderCreateRequestDto.class), eq(buyer))).thenReturn(sampleOrder(5L, 1L));

		mockMvc.perform(post("/api/v1/orders")
						.with(TestSecuritySupport.asUser(buyer))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerName\":\"Buyer\",\"customerEmail\":\"buyer@example.com\",\"status\":\"PENDING\","
								+ "\"items\":[{\"productId\":10,\"quantity\":2}]}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.totalAmount").value(20.00));

		verify(orderService).createOrder(any(OrderCreateRequestDto.class), eq(buyer));
	}

	@Test
	void createOrder_insufficientStock_returnsConflict() throws Exception {
		User buyer = TestSecuritySupport.userWithRole(1L, "buyer@example.com", "USER");
		when(orderService.createOrder(any(OrderCreateRequestDto.class), eq(buyer)))
				.thenThrow(new InsufficientStockException("Insufficient stock for product 'Widget'"));

		mockMvc.perform(post("/api/v1/orders")
						.with(TestSecuritySupport.asUser(buyer))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerName\":\"Buyer\",\"customerEmail\":\"buyer@example.com\",\"status\":\"PENDING\","
								+ "\"items\":[{\"productId\":10,\"quantity\":99}]}"))
				.andExpect(status().isConflict());
	}

	@Test
	void getOrderById_ownedOrder_returnsOk() throws Exception {
		User buyer = TestSecuritySupport.userWithRole(1L, "buyer@example.com", "USER");
		when(orderService.getOrderById(5L, buyer)).thenReturn(sampleOrder(5L, 1L));

		mockMvc.perform(get("/api/v1/orders/5").with(TestSecuritySupport.asUser(buyer)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5));
	}

	@Test
	void getOrderById_notOwned_returnsForbidden() throws Exception {
		User someoneElse = TestSecuritySupport.userWithRole(2L, "someone@example.com", "USER");
		when(orderService.getOrderById(5L, someoneElse))
				.thenThrow(new AccessDeniedException("You do not have access to this order"));

		mockMvc.perform(get("/api/v1/orders/5").with(TestSecuritySupport.asUser(someoneElse)))
				.andExpect(status().isForbidden());
	}

	@Test
	void getOrders_returnsPagedList() throws Exception {
		User owner = TestSecuritySupport.userWithRole(1L, "owner@example.com", "OWNER");
		when(orderService.getAllOrders(eq(owner), any())).thenReturn(new PageImpl<>(List.of(sampleOrder(5L, 9L))));

		mockMvc.perform(get("/api/v1/orders").with(TestSecuritySupport.asUser(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(5));
	}

	@Test
	void unauthenticatedRequestIsRejected() throws Exception {
		mockMvc.perform(get("/api/v1/orders"))
				.andExpect(status().isUnauthorized());
	}
}
