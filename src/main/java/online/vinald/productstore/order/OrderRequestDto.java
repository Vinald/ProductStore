package online.vinald.productstore.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OrderRequestDto(
		@NotBlank(message = "Customer name is required")
		String customerName,

		@NotBlank(message = "Customer email is required")
		@Email(message = "Customer email must be a valid email address")
		String customerEmail,

		String status
) {
}
