package online.vinald.productstore.auth;

public record AuthResponseDto(
		String token,
		String refreshToken,
		String email,
		String role
) {
}
