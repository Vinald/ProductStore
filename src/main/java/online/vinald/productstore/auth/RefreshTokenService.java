package online.vinald.productstore.auth;

import online.vinald.productstore.exception.InvalidCredentialsException;
import online.vinald.productstore.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final long refreshExpirationMs;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
								@Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public RefreshToken createRefreshToken(User user) {
		refreshTokenRepository.deleteByUser(user);
		refreshTokenRepository.flush();

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));

		return refreshTokenRepository.save(refreshToken);
	}

	public RefreshToken verifyRefreshToken(String token) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

		if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
			refreshTokenRepository.delete(refreshToken);
			throw new InvalidCredentialsException("Refresh token has expired");
		}

		return refreshToken;
	}

	public void deleteByToken(String token) {
		refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
	}
}
