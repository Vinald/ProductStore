package online.vinald.productstore.product;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
		return productRepository.findAll(pageable)
				.map(productMapper::toResponseDto);
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
