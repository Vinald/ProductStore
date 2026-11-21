package online.vinald.productstore.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
	private  final ProductService productService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('OWNER')")
	public ProductResponseDto createProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
		return productService.createProduct(productRequestDto);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public Page<ProductResponseDto> getProducts(Pageable pageable) {
		return  productService.getAllProducts(pageable);
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ProductResponseDto getProductById(@PathVariable Long id) {
		return productService.getProductById(id);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('OWNER')")
	public ProductResponseDto updateProduct( @PathVariable Long id, @Valid @RequestBody ProductRequestDto productRequestDto) {
		return productService.updateProduct(id, productRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('OWNER')")
	public void deleteProduct(@PathVariable Long id) {
		productService.deleteProductById(id);
	}
}
