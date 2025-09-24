package co.com.farmatodo.api.dto.shoppingcart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder(toBuilder = true)
public record AddProductDTO(
        @NotBlank(message = "Product ID cannot be empty.")
        String productId,

        @Positive(message = "Quantity must be a positive number.")
        int quantity



) {
}
