package co.com.farmatodo.api.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder(toBuilder = true)
public record CardDTO(
        @NotBlank @Pattern(regexp = "^\\d{13,16}$", message = "Invalid card number")
        String cardNumber,

        @NotBlank @Pattern(regexp = "^\\d{3,4}$", message = "Invalid CVV")
        String cvv,

        @NotBlank @Pattern(regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2})$", message = "Invalid expiration date format MM/YY")
        String expirationDate,

        @NotBlank
        String email
) {}

