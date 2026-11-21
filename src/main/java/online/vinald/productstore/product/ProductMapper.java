package online.vinald.productstore.product;

import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

	public Product toEntity(ProductRequestDto productRequestDto) {
		Product product = new Product();
		updateEntity(product, productRequestDto);
		return product;
	}

	public void updateEntity(Product product, ProductRequestDto productRequestDto) {
		product.setName(productRequestDto.name());
		product.setDescription(productRequestDto.description());
		product.setCategory(productRequestDto.category());
		product.setPrice(productRequestDto.price());
		product.setQuantity(productRequestDto.quantity());
	}

	public ProductResponseDto toResponseDto(Product product) {
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
