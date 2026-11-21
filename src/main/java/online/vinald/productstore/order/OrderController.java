package online.vinald.productstore.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.vinald.productstore.product.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	@PostMapping
	public Order createOrder(@Valid @RequestBody Order order) {
		// TODO: Implement this method
		return null;
	}

	@GetMapping
	public List<Order> getOrders() {
		// TODO: Implement this method
		return null;
	}

	@GetMapping("/{id}")
	public Order getOrderById(@PathVariable Long id) {
		// TODO: Implement this method
		return null;
	}

	@PutMapping("/{id}")
	public Order updateOrder( @PathVariable Long id, @Valid @RequestBody Order order) {
		// TODO: Implement this method
		return null;
	}

	@DeleteMapping("/{id}")
	public void deleteOrder(@PathVariable Long id) {
		// TODO: Implement this method
	}
}
