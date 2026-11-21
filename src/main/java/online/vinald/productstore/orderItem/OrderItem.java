package online.vinald.productstore.orderItem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.vinald.productstore.order.Order;
import online.vinald.productstore.product.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@Data
@AllArgsConstructor
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer quantity;

	@Column(updatable = false, name = "price_at_purchase")
	private BigDecimal priceAtPurchase;

	@Column(updatable = false, name = "created_at")
	private LocalDate createdAt = LocalDate.now();

	@Column(name = "updated_at")
	private LocalDate updatedAt = LocalDate.now();

	private UUID uuid = UUID.randomUUID();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;
}
