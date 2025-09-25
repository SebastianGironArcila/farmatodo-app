package co.com.farmatodo.api.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(toBuilder = true)
@Schema(
        name = "TokenDTO",
        description = "DTO for the generated card token response"
)
public record TokenDTO(
        @Schema(
                description = "Generated token for the card",
                example = "tok_1234567890abcdef"
        )
        String token
) {}
