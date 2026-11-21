package online.vinald.productstore.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponseDto createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
		return orderService.createOrder(orderRequestDto);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<OrderResponseDto> getOrders() {
		return orderService.getAllOrders();
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderResponseDto getOrderById(@PathVariable Long id) {
		return orderService.getOrderById(id);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderResponseDto updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequestDto orderRequestDto) {
		return orderService.updateOrder(id, orderRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOrder(@PathVariable Long id) {
		orderService.deleteOrderById(id);
	}
}
