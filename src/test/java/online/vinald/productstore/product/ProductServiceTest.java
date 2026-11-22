package online.vinald.productstore.product;

import online.vinald.productstore.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	private ProductService productService;

	@BeforeEach
	void setUp() {
		productService = new ProductService(productRepository, new ProductMapper());
	}

	@Test
	void createProduct_savesAndReturnsResponseDto() {
		ProductRequestDto request = new ProductRequestDto("Widget", "desc", "misc", new BigDecimal("10.00"), 5);
		Product saved = new Product();
		saved.setId(1L);
		saved.setName("Widget");
		saved.setPrice(new BigDecimal("10.00"));
		saved.setQuantity(5);

		when(productRepository.save(any(Product.class))).thenReturn(saved);

		ProductResponseDto response = productService.createProduct(request);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Widget");
		verify(productRepository).save(any(Product.class));
	}

	@Test
	void getProductById_found_returnsResponseDto() {
		Product product = new Product();
		product.setId(1L);
		product.setName("Widget");
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		ProductResponseDto response = productService.getProductById(1L);

		assertThat(response.id()).isEqualTo(1L);
	}

	@Test
	void getProductById_notFound_throwsResourceNotFoundException() {
		when(productRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getProductById(99L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAllProducts_mapsPageContent() {
		Product product = new Product();
		product.setId(1L);
		product.setName("Widget");
		Pageable pageable = PageRequest.of(0, 10);
		when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product)));

		Page<ProductResponseDto> result = productService.getAllProducts(pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).name()).isEqualTo("Widget");
	}

	@Test
	void updateProduct_notFound_throwsResourceNotFoundException() {
		when(productRepository.findById(99L)).thenReturn(Optional.empty());
		ProductRequestDto request = new ProductRequestDto("Widget", "desc", "misc", new BigDecimal("10.00"), 5);

		assertThatThrownBy(() -> productService.updateProduct(99L, request))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deleteProductById_notFound_throwsResourceNotFoundException() {
		when(productRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> productService.deleteProductById(99L))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(productRepository, never()).deleteById(any());
	}

	@Test
	void deleteProductById_found_deletes() {
		when(productRepository.existsById(1L)).thenReturn(true);

		productService.deleteProductById(1L);

		verify(productRepository).deleteById(1L);
	}
}
