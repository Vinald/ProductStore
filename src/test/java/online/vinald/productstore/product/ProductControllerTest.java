package online.vinald.productstore.product;

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
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Test
	void ownerCanCreateProduct() throws Exception {
		String token = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "owner1@example.com", "OWNER");

		mockMvc.perform(post("/api/v1/products")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Widget\",\"price\":10.00,\"quantity\":5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Widget"));
	}

	@Test
	void regularUserCannotCreateProduct() throws Exception {
		String token = TestAuthSupport.registerAndGetToken(mockMvc, userRepository, "user1@example.com", "USER");

		mockMvc.perform(post("/api/v1/products")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Widget\",\"price\":10.00,\"quantity\":5}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void unauthenticatedRequestIsRejected() throws Exception {
		mockMvc.perform(get("/api/v1/products"))
				.andExpect(status().isUnauthorized());
	}
}
