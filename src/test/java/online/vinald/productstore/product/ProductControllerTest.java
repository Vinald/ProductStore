package online.vinald.productstore.product;

import online.vinald.productstore.TestSecuritySupport;
import online.vinald.productstore.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

	@MockitoBean
	private ProductService productService;

	@Test
	void ownerCanCreateProduct() throws Exception {
		User owner = TestSecuritySupport.userWithRole(1L, "owner@example.com", "OWNER");
		ProductResponseDto response = new ProductResponseDto(1L, "Widget", "desc", "misc",
				new BigDecimal("10.00"), 5, null, null, null);
		when(productService.createProduct(any(ProductRequestDto.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/products")
						.with(TestSecuritySupport.asUser(owner))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Widget\",\"price\":10.00,\"quantity\":5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Widget"));
	}

	@Test
	void regularUserCannotCreateProduct() throws Exception {
		User user = TestSecuritySupport.userWithRole(2L, "user@example.com", "USER");

		mockMvc.perform(post("/api/v1/products")
						.with(TestSecuritySupport.asUser(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Widget\",\"price\":10.00,\"quantity\":5}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void anyAuthenticatedUserCanListProducts() throws Exception {
		User user = TestSecuritySupport.userWithRole(2L, "user@example.com", "USER");
		ProductResponseDto response = new ProductResponseDto(1L, "Widget", "desc", "misc",
				new BigDecimal("10.00"), 5, null, null, null);
		when(productService.getAllProducts(any())).thenReturn(new PageImpl<>(List.of(response)));

		mockMvc.perform(get("/api/v1/products").with(TestSecuritySupport.asUser(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("Widget"));
	}

	@Test
	void unauthenticatedRequestIsRejected() throws Exception {
		mockMvc.perform(get("/api/v1/products"))
				.andExpect(status().isUnauthorized());
	}
}
