package online.vinald.productstore.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	@Test
	void register_delegatesToAuthServiceAndReturnsCreated() throws Exception {
		AuthResponseDto mockResponse = new AuthResponseDto("access-token", "refresh-token", "alice@example.com", "USER");
		when(authService.register(any(AuthRequestDto.class))).thenReturn(mockResponse);

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("alice@example.com"))
				.andExpect(jsonPath("$.token").value("access-token"));
	}

	@Test
	void login_delegatesToAuthServiceAndReturnsOk() throws Exception {
		AuthResponseDto mockResponse = new AuthResponseDto("access-token", "refresh-token", "alice@example.com", "USER");
		when(authService.login(any(AuthRequestDto.class))).thenReturn(mockResponse);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("access-token"));
	}

	@Test
	void refresh_delegatesToAuthServiceWithRefreshTokenValue() throws Exception {
		AuthResponseDto mockResponse = new AuthResponseDto("new-access-token", "refresh-token", "alice@example.com", "USER");
		when(authService.refresh("refresh-token")).thenReturn(mockResponse);

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"refresh-token\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("new-access-token"));
	}

	@Test
	void logout_delegatesToRefreshTokenServiceAndReturnsNoContent() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"refresh-token\"}"))
				.andExpect(status().isNoContent());

		verify(refreshTokenService).deleteByToken("refresh-token");
	}
}
