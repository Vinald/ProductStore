package online.vinald.productstore.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequestDto(
		@NotBlank(message = "Customer name is required")
		String customerName,

		@NotBlank(message = "Customer email is required")
		@Email(message = "Customer email must be a valid email address")
		String customerEmail,

		String status,

		@NotNull(message = "Total amount is required")
		@DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than zero")
		BigDecimal totalAmount
) {
}
