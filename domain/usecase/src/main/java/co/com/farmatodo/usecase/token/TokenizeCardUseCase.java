package co.com.farmatodo.usecase.token;

import co.com.farmatodo.model.card.Card;
import co.com.farmatodo.model.common.exception.BusinessException;
import co.com.farmatodo.model.token.Token;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Random;

@RequiredArgsConstructor
public class TokenizeCardUseCase {

    // Esta propiedad vendrá del application.yml
    private final double rejectionProbability;
    private final Random random = new Random();
    private static final String CARD_TYPE_VISA= "VISA";
    private static final String CARD_TYPE_MASTERCARD= "MASTERCARD";
    private static final String CARD_TYPE_VISA_CODE = "4";
    private static final String CARD_TYPE_MASTERCARD_CODE = "5";



    public Mono<Token> tokenize(Card card) {
        // 1. Simular la probabilidad de rechazo
        if (random.nextDouble() < rejectionProbability) {
            // Aquí puedes usar tu BusinessException como en RegisterClientUseCase
            return Mono.error(BusinessException.Type.TOKENIZATION_REJECTED_BY_PROBABILITY.build());
        }

        // 2. Lógica de tokenización (ejemplo simple)
        // En un caso real, esto sería más complejo y seguro.
        String originalInput = card.getCardNumber() + ":" + card.getEmail();
        String tokenValue = Base64.getEncoder().encodeToString(originalInput.getBytes());

        // 3. Determinar el tipo de tarjeta (lógica de ejemplo)
        String cardType = "UNKNOWN";
        if (card.getCardNumber().startsWith(CARD_TYPE_VISA_CODE)) {
            cardType = CARD_TYPE_VISA;
        } else if (card.getCardNumber().startsWith(CARD_TYPE_MASTERCARD_CODE)) {
            cardType = CARD_TYPE_MASTERCARD;
        }

        Token token = new Token(tokenValue, cardType, LocalDateTime.now());

        return Mono.just(token);
    }
}
