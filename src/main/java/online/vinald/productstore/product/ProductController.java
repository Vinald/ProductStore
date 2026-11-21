package online.vinald.productstore.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
	private  final ProductService productService;

	@PostMapping
	public Product createProduct(@Valid @RequestBody Product product) {
		// TODO: Implement this method
		return null;
	}

	@GetMapping
	public List<Product> getProducts() {
		// TODO: Implement this method
		return null;
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
		// TODO: Implement this method
		return null;
	}

	@PutMapping("/{id}")
	public Product updateProduct( @PathVariable Long id, @Valid @RequestBody Product product) {
		// TODO: Implement this method
		return null;
	}

	@DeleteMapping("/{id}")
	public void deleteProduct(@PathVariable Long id) {
		// TODO: Implement this method
	}
}
