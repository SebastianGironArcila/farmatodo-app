package co.com.farmatodo.api.dto.card;

import lombok.Builder;

@Builder(toBuilder = true)
public record TokenDTO (String token){
}
