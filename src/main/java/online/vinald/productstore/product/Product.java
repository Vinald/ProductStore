package online.vinald.productstore.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@NotBlank(message = "Name is required")
	private String name;

	private  String description;

	private String category;

	@Column(nullable = false)
	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
	private BigDecimal price;

	@Column(nullable = false)
	@NotNull(message = "Quantity is required")
	@Min(value = 0, message = "Quantity must be a non-negative number")
	private Integer quantity;

	@Column(updatable = false, name = "created_at")
	private LocalDate createdAt = LocalDate.now();

	@Column(name = "updated_at")
	private LocalDate updatedAt = LocalDate.now();

	private UUID uuid = UUID.randomUUID();
}
