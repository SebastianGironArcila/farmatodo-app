package co.com.farmatodo.api.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder(toBuilder = true)
@Schema(
        name = "CardDTO",
        description = "DTO for credit card tokenization"
)
public record CardDTO(
        @Schema(
                description = "Credit card number (13 to 16 digits)",
                example = "4111111111111111",
                pattern = "^\\d{13,16}$"
        )
        @NotBlank
        @Pattern(regexp = "^\\d{13,16}$", message = "Invalid card number")
        String cardNumber,

        @Schema(
                description = "Card CVV (3 or 4 digits)",
                example = "123",
                pattern = "^\\d{3,4}$"
        )
        @NotBlank
        @Pattern(regexp = "^\\d{3,4}$", message = "Invalid CVV")
        String cvv,

        @Schema(
                description = "Expiration date in MM/YY format",
                example = "12/25",
                pattern = "^(0[1-9]|1[0-2])/([0-9]{2})$"
        )
        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Invalid expiration date format MM/YY")
        String expirationDate,

        @Schema(
                description = "Email associated with the card",
                example = "john.doe@email.com"
        )
        @NotBlank
        String email
) {}
