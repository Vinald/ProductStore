package online.vinald.productstore.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.vinald.productstore.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponseDto createOrder(@Valid @RequestBody OrderCreateRequestDto orderCreateRequestDto,
										 @AuthenticationPrincipal User currentUser) {
		return orderService.createOrder(orderCreateRequestDto, currentUser);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public Page<OrderResponseDto> getOrders(Pageable pageable, @AuthenticationPrincipal User currentUser) {
		return orderService.getAllOrders(currentUser, pageable);
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderResponseDto getOrderById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
		return orderService.getOrderById(id, currentUser);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderResponseDto updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequestDto orderRequestDto,
										 @AuthenticationPrincipal User currentUser) {
		return orderService.updateOrder(id, orderRequestDto, currentUser);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOrder(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
		orderService.deleteOrderById(id, currentUser);
	}
}
