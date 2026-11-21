package online.vinald.productstore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
	private  final ProductRepository productRepository;
	private final ProductMapper productMapper;

	public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
		Product product = productMapper.toEntity(productRequestDto);
		return productMapper.toResponseDto(productRepository.save(product));
	}

	public List<ProductResponseDto> getAllProducts() {
		return productRepository.findAll().stream()
				.map(productMapper::toResponseDto)
				.toList();
	}

	public ProductResponseDto getProductById(Long id) {
		return productRepository.findById(id)
				.map(productMapper::toResponseDto)
				.orElseThrow(() -> new RuntimeException("Product not found"));
	}

	public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		productMapper.updateEntity(existingProduct, productRequestDto);

		return productMapper.toResponseDto(productRepository.save(existingProduct));
	}

	public  void deleteProductById(Long id) {
		if (!productRepository.existsById(id)) {
			throw new RuntimeException("Product not found");
		}
		productRepository.deleteById(id);
	}

}
