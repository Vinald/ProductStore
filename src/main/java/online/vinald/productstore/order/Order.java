package online.vinald.productstore.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.vinald.productstore.orderItem.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Customer name is required")
	@Column(nullable = false, name = "customer_name")
	private String customerName;

	@NotNull(message = "Customer email is required")
	@Column(nullable = false, name = "customer_email")
	private String customerEmail;

	private String status;

	@Column(nullable = false, name = "total_amount")
	private BigDecimal totalAmount;

	@Column(updatable = false)
	private LocalDate createdAt;

	@Column(updatable = false)
	private LocalDate updatedAt;

	private UUID uuid;

	@JsonManagedReference
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems;

	@PrePersist
	protected void onCreate() {
		this.uuid = UUID.randomUUID();
		this.createdAt = LocalDate.now();
		this.updatedAt = LocalDate.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDate.now();
	}
}
