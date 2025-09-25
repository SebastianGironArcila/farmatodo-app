package co.com.farmatodo.api.dto.shoppingcart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder(toBuilder = true)
@Schema(
        name = "AddProductDTO",
        description = "DTO for adding a product to the shopping cart"
)
public record AddProductDTO(
        @Schema(
                description = "ID of the product to add",
                example = "PROD-001"
        )
        @NotBlank(message = "Product ID cannot be empty.")
        String productId,

        @Schema(
                description = "Quantity of the product to add (must be positive)",
                example = "2",
                minimum = "1"
        )
        @Positive(message = "Quantity must be a positive number.")
        int quantity
) {}
