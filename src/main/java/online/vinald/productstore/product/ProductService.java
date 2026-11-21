package online.vinald.productstore.product;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
	private  final ProductRepository productRepository;
	private final ProductMapper productMapper;

	public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
		Product product = productMapper.toEntity(productRequestDto);
		return productMapper.toResponseDto(productRepository.save(product));
	}

	@Transactional(readOnly = true)
	public List<ProductResponseDto> getAllProducts() {
		return productRepository.findAll().stream()
				.map(productMapper::toResponseDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductResponseDto getProductById(Long id) {
		return productRepository.findById(id)
				.map(productMapper::toResponseDto)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
	}

	public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		productMapper.updateEntity(existingProduct, productRequestDto);

		return productMapper.toResponseDto(productRepository.save(existingProduct));
	}

	public  void deleteProductById(Long id) {
		if (!productRepository.existsById(id)) {
			throw new ResourceNotFoundException("Product not found");
		}
		productRepository.deleteById(id);
	}

}
