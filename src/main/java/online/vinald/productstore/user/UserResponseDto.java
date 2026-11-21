package online.vinald.productstore.user;

import java.time.LocalDate;

public record UserResponseDto(
		Long id,
		String email,
		String displayName,
		String role,
		LocalDate createdAt,
		LocalDate updatedAt
) {
}
