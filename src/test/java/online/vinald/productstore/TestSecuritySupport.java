package online.vinald.productstore;

import online.vinald.productstore.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class TestSecuritySupport {
	private TestSecuritySupport() {
	}

	public static User userWithRole(long id, String email, String role) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setRole(role);
		return user;
	}

	public static RequestPostProcessor asUser(User user) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		return SecurityMockMvcRequestPostProcessors.authentication(authentication);
	}
}
