package online.vinald.productstore.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductResponseDto(
		Long id,
		String name,
		String description,
		String category,
		BigDecimal price,
		Integer quantity,
		LocalDate createdAt,
		LocalDate updatedAt,
		UUID uuid
) {
}
