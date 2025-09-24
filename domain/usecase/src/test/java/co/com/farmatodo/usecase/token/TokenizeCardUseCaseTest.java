package co.com.farmatodo.usecase.token;

import co.com.farmatodo.model.card.Card;
import co.com.farmatodo.model.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class TokenizeCardUseCaseTest {

    private TokenizeCardUseCase useCase;

    @BeforeEach
    void setUp() {
        // Probabilidad 0 → nunca rechaza
        useCase = new TokenizeCardUseCase(0.0);
    }

    @Test
    void mustRejectTokenizationByProbability() {
        // Probabilidad 1.0 → siempre rechaza
        TokenizeCardUseCase rejectingUseCase = new TokenizeCardUseCase(1.0);

        Card card = Card.builder()
                .cardNumber("4111111111111111")
                .email("user@example.com")
                .build();

        StepVerifier.create(rejectingUseCase.tokenize(card))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getCode()
                                .equals(BusinessException.Type.TOKENIZATION_REJECTED_BY_PROBABILITY.name()))
                .verify();
    }

    @Test
    void mustGenerateTokenSuccessfully() {
        Card card = Card.builder()
                .cardNumber("4111111111111111")
                .email("user@example.com")
                .build();

        String expectedValue = Base64.getEncoder()
                .encodeToString((card.getCardNumber() + ":" + card.getEmail()).getBytes());

        StepVerifier.create(useCase.tokenize(card))
                .assertNext(token -> {
                    assertThat(token.getValue()).isEqualTo(expectedValue);
                    assertThat(token.getCardType()).isEqualTo("VISA");
                    assertThat(token.getCreationDate()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void mustDetectVisaCardType() {
        Card visaCard = Card.builder()
                .cardNumber("4111111111111111")
                .email("visa@example.com")
                .build();

        StepVerifier.create(useCase.tokenize(visaCard))
                .assertNext(token -> assertThat(token.getCardType()).isEqualTo("VISA"))
                .verifyComplete();
    }

    @Test
    void mustDetectMasterCardType() {
        Card mastercard = Card.builder()
                .cardNumber("5111111111111111")
                .email("mc@example.com")
                .build();

        StepVerifier.create(useCase.tokenize(mastercard))
                .assertNext(token -> assertThat(token.getCardType()).isEqualTo("MASTERCARD"))
                .verifyComplete();
    }

    @Test
    void mustReturnUnknownForOtherCardTypes() {
        Card other = Card.builder()
                .cardNumber("6111111111111111")
                .email("other@example.com")
                .build();

        StepVerifier.create(useCase.tokenize(other))
                .assertNext(token -> assertThat(token.getCardType()).isEqualTo("UNKNOWN"))
                .verifyComplete();
    }
}
