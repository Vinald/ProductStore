package online.vinald.productstore.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
	private  final ProductService productService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponseDto createProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
		return productService.createProduct(productRequestDto);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<ProductResponseDto> getProducts() {
		return  productService.getAllProducts();
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ProductResponseDto getProductById(@PathVariable Long id) {
		return productService.getProductById(id);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ProductResponseDto updateProduct( @PathVariable Long id, @Valid @RequestBody ProductRequestDto productRequestDto) {
		return productService.updateProduct(id, productRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProduct(@PathVariable Long id) {
		productService.deleteProductById(id);
	}
}
