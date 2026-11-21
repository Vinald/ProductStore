package online.vinald.productstore.orderItem;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemController {
	private  final OrderItemService orderItemService;

	@PostMapping
	public void createOrderItem(@RequestBody OrderItem orderItem) {
	    //TODO: Implement the logic to create an order item
	}
}
