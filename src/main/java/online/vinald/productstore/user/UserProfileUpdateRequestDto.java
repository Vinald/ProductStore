package online.vinald.productstore.user;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequestDto(
		@Size(min = 1, max = 100, message = "Display name must be 1-100 characters")
		String displayName
) {
}
