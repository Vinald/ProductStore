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
	public static ProductResponseDto from(Product product) {
		return new ProductResponseDto(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getCategory(),
				product.getPrice(),
				product.getQuantity(),
				product.getCreatedAt(),
				product.getUpdatedAt(),
				product.getUuid()
		);
	}
}
