package online.vinald.productstore.auth;

import online.vinald.productstore.TestAuthSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void registerThenLoginSucceeds() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("alice@example.com"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void registerWithDuplicateEmailReturnsConflict() throws Exception {
		String body = "{\"email\":\"bob@example.com\",\"password\":\"password123\"}";

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"carol@example.com\",\"password\":\"password123\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"carol@example.com\",\"password\":\"wrongpass\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshThenLogoutInvalidatesRefreshToken() throws Exception {
		String response = mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"dave@example.com\",\"password\":\"password123\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String refreshToken = TestAuthSupport.extractJsonField(response, "refreshToken");

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());

		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isUnauthorized());
	}
}
