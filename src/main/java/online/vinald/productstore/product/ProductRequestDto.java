package online.vinald.productstore.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequestDto(
		@NotBlank(message = "Name is required")
		String name,

		String description,

		String category,

		@NotNull(message = "Price is required")
		@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
		BigDecimal price,

		@NotNull(message = "Quantity is required")
		@Min(value = 0, message = "Quantity must be a non-negative number")
		Integer quantity
) {
}
