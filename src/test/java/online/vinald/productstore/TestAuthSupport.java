package online.vinald.productstore;

import online.vinald.productstore.user.User;
import online.vinald.productstore.user.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestAuthSupport {
	private TestAuthSupport() {
	}

	public static String registerAndGetToken(MockMvc mockMvc, UserRepository userRepository, String email, String role) throws Exception {
		String response = mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		if (!"USER".equals(role)) {
			User user = userRepository.findByEmail(email).orElseThrow();
			user.setRole(role);
			userRepository.save(user);
		}

		return extractJsonField(response, "token");
	}

	public static String extractJsonField(String json, String field) {
		return json.replaceAll(".*\"" + field + "\":\"([^\"]*)\".*", "$1");
	}

	public static Long extractJsonLongField(String json, String field) {
		return Long.valueOf(json.replaceAll(".*\"" + field + "\":(\\d+).*", "$1"));
	}
}
