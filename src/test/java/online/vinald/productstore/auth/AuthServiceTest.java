package online.vinald.productstore.auth;

import online.vinald.productstore.exception.DuplicateResourceException;
import online.vinald.productstore.exception.InvalidCredentialsException;
import online.vinald.productstore.user.User;
import online.vinald.productstore.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private RefreshTokenService refreshTokenService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, refreshTokenService);
	}

	private RefreshToken refreshTokenFor(User user, String token) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(token);
		refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
		return refreshToken;
	}

	@Test
	void register_newEmail_savesUserAndReturnsTokens() {
		AuthRequestDto request = new AuthRequestDto("jane@example.com", "password123");
		when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
		when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
		when(refreshTokenService.createRefreshToken(any(User.class)))
				.thenAnswer(invocation -> refreshTokenFor(invocation.getArgument(0), "refresh-token"));

		AuthResponseDto response = authService.register(request);

		assertThat(response.token()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
		assertThat(response.email()).isEqualTo("jane@example.com");
		assertThat(response.role()).isEqualTo("USER");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void register_duplicateEmail_throwsDuplicateResourceExceptionAndDoesNotSave() {
		AuthRequestDto request = new AuthRequestDto("jane@example.com", "password123");
		when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateResourceException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void login_validCredentials_returnsTokens() {
		AuthRequestDto request = new AuthRequestDto("jane@example.com", "password123");
		User user = new User();
		user.setId(1L);
		user.setEmail("jane@example.com");
		user.setRole("USER");

		when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
		when(jwtService.generateToken(user)).thenReturn("access-token");
		when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshTokenFor(user, "refresh-token"));

		AuthResponseDto response = authService.login(request);

		assertThat(response.token()).isEqualTo("access-token");
		assertThat(response.email()).isEqualTo("jane@example.com");
	}

	@Test
	void login_badCredentials_throwsInvalidCredentialsException() {
		AuthRequestDto request = new AuthRequestDto("jane@example.com", "wrong-password");
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);

		verify(userRepository, never()).findByEmail(anyString());
	}

	@Test
	void refresh_validToken_returnsNewAccessTokenWithSameRefreshToken() {
		User user = new User();
		user.setId(1L);
		user.setEmail("jane@example.com");
		user.setRole("USER");
		RefreshToken refreshToken = refreshTokenFor(user, "refresh-token");

		when(refreshTokenService.verifyRefreshToken("refresh-token")).thenReturn(refreshToken);
		when(jwtService.generateToken(user)).thenReturn("new-access-token");

		AuthResponseDto response = authService.refresh("refresh-token");

		assertThat(response.token()).isEqualTo("new-access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
		assertThat(response.email()).isEqualTo("jane@example.com");
	}
}
