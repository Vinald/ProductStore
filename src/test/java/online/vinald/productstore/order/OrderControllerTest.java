package online.vinald.productstore.order;

import online.vinald.productstore.TestAuthSupport;
import online.vinald.productstore.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

	@Autowired
	private UserRepository userRepository;

	private Long createProductAsOwner(String ownerToken, int quantity) throws Exception {
		String response = mockMvc.perform(post("/api/v1/products")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Widget\",\"price\":10.00,\"quantity\":" + quantity + "}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return TestAuthSupport.extractJsonLongField(response, "id");
	}

	@Test
	void customerCanOnlySeeOwnOrders() throws Exception {
		String ownerToken = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "owner2@example.com", "OWNER");
		Long productId = createProductAsOwner(ownerToken, 10);

		String buyer1Token = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "buyer1@example.com", "USER");
		String buyer2Token = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "buyer2@example.com", "USER");

		String orderBody = "{\"customerName\":\"Buyer One\",\"customerEmail\":\"buyer1@example.com\",\"status\":\"PENDING\","
				+ "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}";

		String orderResponse = mockMvc.perform(post("/api/v1/orders")
						.header("Authorization", "Bearer " + buyer1Token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.totalAmount").value(10.00))
				.andReturn().getResponse().getContentAsString();

		Long orderId = TestAuthSupport.extractJsonLongField(orderResponse, "id");

		mockMvc.perform(get("/api/v1/orders/" + orderId).header("Authorization", "Bearer " + buyer2Token))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/orders/" + orderId).header("Authorization", "Bearer " + buyer1Token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer " + buyer2Token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0));

		mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1));
	}

	@Test
	void orderingMoreThanAvailableStockFails() throws Exception {
		String ownerToken = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "owner3@example.com", "OWNER");
		Long productId = createProductAsOwner(ownerToken, 1);

		String buyerToken = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "buyer3@example.com", "USER");

		String orderBody = "{\"customerName\":\"Buyer\",\"customerEmail\":\"buyer3@example.com\",\"status\":\"PENDING\","
				+ "\"items\":[{\"productId\":" + productId + ",\"quantity\":5}]}";

		mockMvc.perform(post("/api/v1/orders")
						.header("Authorization", "Bearer " + buyerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderBody))
				.andExpect(status().isConflict());
	}
}
