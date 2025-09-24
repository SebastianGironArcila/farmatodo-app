package co.com.farmatodo.api;

import co.com.farmatodo.api.config.GlobalExceptionHandler;
import co.com.farmatodo.api.config.ValidationHandler;
import co.com.farmatodo.api.dto.card.CardDTO;
import co.com.farmatodo.api.dto.card.TokenDTO;
import co.com.farmatodo.api.dto.client.CreateClientDTO;
import co.com.farmatodo.api.dto.shoppingcart.AddProductDTO;
import co.com.farmatodo.api.mapper.CardDTOMapper;
import co.com.farmatodo.api.mapper.ClientDTOMapper;
import co.com.farmatodo.model.card.Card;
import co.com.farmatodo.model.cart.ShoppingCart;
import co.com.farmatodo.model.client.Client;
import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.model.token.Token;
import co.com.farmatodo.usecase.cart.AddProductToCartUseCase;
import co.com.farmatodo.usecase.client.RegisterClientUseCase;
import co.com.farmatodo.usecase.product.SearchProductsUseCase;
import co.com.farmatodo.usecase.token.TokenizeCardUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, GlobalExceptionHandler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RegisterClientUseCase registerClientUseCase;

    @MockitoBean
    private TokenizeCardUseCase tokenizeCardUseCase;

    @MockitoBean
    private SearchProductsUseCase searchProductsUseCase;

    @MockitoBean
    private AddProductToCartUseCase addProductToCartUseCase;

    @MockitoBean
    private ClientDTOMapper clientDTOMapper;

    @MockitoBean
    private CardDTOMapper cardDTOMapper;

    @MockitoBean
    private ValidationHandler validationHandler;



    private final CreateClientDTO validClientDTO = CreateClientDTO.builder()
            .name("John Doe")
            .email("john@test.com")
            .phone("1234567890")
            .address("Street 123")
            .build();

    private final Client client = Client.builder()
            .email("john@test.com")
            .name("John")
            .build();

    private final CardDTO cardDTO = CardDTO.builder()
            .cardNumber("4111111111111111")
            .cvv("123")
            .expirationDate("12/25")
            .email("john@test.com")
            .build();

    private final Card card = Card.builder()
            .cardNumber("4111111111111111")
            .cvv("123")
            .expirationDate("12/25")
            .build();

    private final TokenDTO tokenDTO= TokenDTO.builder()
            .build();

    private final Token token = Token.builder()
            .build();

    private final Product product = Product.builder()
            .id("1")
            .name("Paracetamol")
            .price(BigDecimal.valueOf(2000.0))
            .build();

    private final AddProductDTO addProductDTO = AddProductDTO.builder()
            .productId("1")
            .quantity(2)
            .build();

    private final ShoppingCart shoppingCart = ShoppingCart.builder()
            .clientId("client123")
            .build();

    @Test
    void shouldReturnPing() {
        webTestClient.get()
                .uri("/api/v1/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("pong");
    }

    @Test
    void shouldRegisterClientSuccessfully() {
        when(validationHandler.validate(any(CreateClientDTO.class))).thenReturn(Mono.just(validClientDTO));
        when(clientDTOMapper.toModel(any(CreateClientDTO.class))).thenReturn(client);
        when(registerClientUseCase.register(any(Client.class))).thenReturn(Mono.just(client));

        webTestClient.post()
                .uri("/api/v1/client")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validClientDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class)
                .value(c -> Assertions.assertThat(c.getEmail()).isEqualTo("john@test.com"));
    }

    @Test
    void whenRegisterClientValidationFails_thenReturnBadRequest() {
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(PathImpl.createPathFromString("email"));
        Mockito.when(violation.getMessage()).thenReturn("Email is required");

        Set<ConstraintViolation<?>> violations = Set.of(violation);
        when(validationHandler.validate(any(CreateClientDTO.class)))
                .thenReturn(Mono.error(new ConstraintViolationException("Validation failed", violations)));

        webTestClient.post()
                .uri("/api/v1/client")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validClientDTO.toBuilder().email("").build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_FAILED")
                .jsonPath("$.message[0]").isEqualTo("email: Email is required");
    }

    @Test
    void shouldTokenizeCardSuccessfully() {
        when(validationHandler.validate(any(CardDTO.class))).thenReturn(Mono.just(cardDTO));
        when(cardDTOMapper.toModel(any(CardDTO.class))).thenReturn(card);
        when(tokenizeCardUseCase.tokenize(any(Card.class))).thenReturn(Mono.just(token));

        webTestClient.post()
                .uri("/api/v1/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cardDTO)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldSearchProductsSuccessfully() {
        when(searchProductsUseCase.searchByName(anyString())).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/v1/products?name=Paracetamol")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .value(list -> {
                    Assertions.assertThat(list).hasSize(1);
                    Assertions.assertThat(list.get(0).getName()).isEqualTo("Paracetamol");
                });
    }

    @Test
    void shouldAddProductToCartSuccessfully() {
        when(validationHandler.validate(any(AddProductDTO.class))).thenReturn(Mono.just(addProductDTO));
        when(addProductToCartUseCase.addProduct(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(shoppingCart));

        webTestClient.post()
                .uri("/api/v1/cart/client123/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(addProductDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ShoppingCart.class)
                .value(cart -> Assertions.assertThat(cart.getClientId()).isEqualTo("client123"));
    }
}
