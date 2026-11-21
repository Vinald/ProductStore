package online.vinald.productstore.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import online.vinald.productstore.orderItem.OrderItemCreateDto;

import java.util.List;

public record OrderCreateRequestDto(
		@NotBlank(message = "Customer name is required")
		String customerName,

		@NotBlank(message = "Customer email is required")
		@Email(message = "Customer email must be a valid email address")
		String customerEmail,

		String status,

		@NotEmpty(message = "An order must have at least one item")
		@Valid
		List<OrderItemCreateDto> items
) {
}
