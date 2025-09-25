package co.com.farmatodo.api.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder(toBuilder = true)
@Schema(
        name = "CreateClientDTO",
        description = "DTO for registering a new client"
)
public record CreateClientDTO(
        @Schema(
                description = "Full name of the client",
                example = "John Doe"
        )
        @NotBlank(message = "Name is required")
        String name,

        @Schema(
                description = "Client's email address",
                example = "john.doe@email.com"
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(
                description = "Client's phone number (10 digits)",
                example = "3001234567",
                pattern = "\\d{10}"
        )
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits")
        String phone,

        @Schema(
                description = "Client's address",
                example = "123 Main St, Apt 4B"
        )
        @NotBlank(message = "Address is required")
        String address
) {}
