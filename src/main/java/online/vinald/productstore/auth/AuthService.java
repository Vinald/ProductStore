package online.vinald.productstore.auth;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.exception.DuplicateResourceException;
import online.vinald.productstore.exception.InvalidCredentialsException;
import online.vinald.productstore.user.User;
import online.vinald.productstore.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	public AuthResponseDto register(AuthRequestDto authRequestDto) {
		if (userRepository.existsByEmail(authRequestDto.email())) {
			throw new DuplicateResourceException("Email '" + authRequestDto.email() + "' is already registered");
		}

		User user = new User();
		user.setEmail(authRequestDto.email());
		user.setPassword(passwordEncoder.encode(authRequestDto.password()));
		user.setRole("USER");
		user.setProvider("LOCAL");
		userRepository.save(user);

		return buildAuthResponse(user);
	}

	public AuthResponseDto login(AuthRequestDto authRequestDto) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequestDto.email(), authRequestDto.password()));
		} catch (BadCredentialsException ex) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmail(authRequestDto.email())
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		return buildAuthResponse(user);
	}

	public AuthResponseDto refresh(String refreshTokenValue) {
		RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
		User user = refreshToken.getUser();

		String newAccessToken = jwtService.generateToken(user);

		return new AuthResponseDto(newAccessToken, refreshTokenValue, user.getEmail(), user.getRole());
	}

	private AuthResponseDto buildAuthResponse(User user) {
		String accessToken = jwtService.generateToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
		return new AuthResponseDto(accessToken, refreshToken.getToken(), user.getEmail(), user.getRole());
	}
}
