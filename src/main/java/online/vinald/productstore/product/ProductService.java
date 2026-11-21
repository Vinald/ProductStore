package online.vinald.productstore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
	private  final ProductRepository productRepository;

	public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
		Product product = new Product();
		product.setName(productRequestDto.name());
		product.setDescription(productRequestDto.description());
		product.setCategory(productRequestDto.category());
		product.setPrice(productRequestDto.price());
		product.setQuantity(productRequestDto.quantity());
		return ProductResponseDto.from(productRepository.save(product));
	}

	public List<ProductResponseDto> getAllProducts() {
		return productRepository.findAll().stream()
				.map(ProductResponseDto::from)
				.toList();
	}

	public ProductResponseDto getProductById(Long id) {
		return productRepository.findById(id)
				.map(ProductResponseDto::from)
				.orElseThrow(() -> new RuntimeException("Product not found"));
	}

	public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		existingProduct.setName(productRequestDto.name());
		existingProduct.setDescription(productRequestDto.description());
		existingProduct.setCategory(productRequestDto.category());
		existingProduct.setPrice(productRequestDto.price());
		existingProduct.setQuantity(productRequestDto.quantity());

		return ProductResponseDto.from(productRepository.save(existingProduct));
	}

	public  void deleteProductById(Long id) {
		if (!productRepository.existsById(id)) {
			throw new RuntimeException("Product not found");
		}
		productRepository.deleteById(id);
	}

}
