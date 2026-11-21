package online.vinald.productstore.orderItem;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/order-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OrderItemController {
	private final OrderItemService orderItemService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderItemResponseDto createOrderItem(@Valid @RequestBody OrderItemRequestDto orderItemRequestDto) {
		return orderItemService.createOrderItem(orderItemRequestDto);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public Page<OrderItemResponseDto> getOrderItems(Pageable pageable) {
		return orderItemService.getAllOrderItems(pageable);
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderItemResponseDto getOrderItemById(@PathVariable Long id) {
		return orderItemService.getOrderItemById(id);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderItemResponseDto updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItemRequestDto orderItemRequestDto) {
		return orderItemService.updateOrderItem(id, orderItemRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOrderItem(@PathVariable Long id) {
		orderItemService.deleteOrderItemById(id);
	}
}
