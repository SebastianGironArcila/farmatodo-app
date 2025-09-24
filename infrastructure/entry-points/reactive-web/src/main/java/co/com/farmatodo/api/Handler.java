package co.com.farmatodo.api;

import co.com.farmatodo.api.config.ValidationHandler;
import co.com.farmatodo.api.dto.card.CardDTO;
import co.com.farmatodo.api.dto.client.CreateClientDTO;
import co.com.farmatodo.api.dto.shoppingcart.AddProductDTO;
import co.com.farmatodo.api.mapper.CardDTOMapper;
import co.com.farmatodo.api.mapper.ClientDTOMapper;
import co.com.farmatodo.model.card.Card;
import co.com.farmatodo.model.client.Client;
import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.usecase.cart.AddProductToCartUseCase;
import co.com.farmatodo.usecase.client.RegisterClientUseCase;
import co.com.farmatodo.usecase.product.SearchProductsUseCase;
import co.com.farmatodo.usecase.token.TokenizeCardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Tag(name = "Client", description = "Endpoints for client management")
@Slf4j
public class Handler {

    private final RegisterClientUseCase registerclientUseCase;
    private final TokenizeCardUseCase tokenizeCardUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final AddProductToCartUseCase addProductToCartUseCase;
    private final ClientDTOMapper clientDTOMapper;
    private final CardDTOMapper cardDTOMapper;
    private final ValidationHandler validationHandler;



    public Mono<ServerResponse> ping(ServerRequest serverRequest) {
        return ServerResponse.ok().bodyValue("pong");
    }

    @Operation(summary = "Register a new client")
    public Mono<ServerResponse> registerClient(ServerRequest request) {
        log.info("Starting user registration process");

        return request.bodyToMono(CreateClientDTO.class)
                .doOnNext(dto -> log.info("Registration request for email: {}", dto.email()))
                .flatMap(validationHandler::validate)
                .flatMap(dto -> {
                    Client client = clientDTOMapper.toModel(dto);
                    return registerclientUseCase.register(client);
                })
                .doOnNext(user -> log.info("Client registered successfully - ID: {}", user.getEmail()))
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .doOnError(error -> log.error("Registration failed: {}", error.getMessage()))
                .doOnSuccess(response -> log.info("Registration process completed"));
    }


    @Operation(summary = "Tokenize a credit card")
    public Mono<ServerResponse> tokenizeCard(ServerRequest request) {
        log.info("Starting tokenizar credit cardprocess");

        return request.bodyToMono(CardDTO.class)
                .flatMap(validationHandler::validate)
                .flatMap(dto -> {Card card = cardDTOMapper.toModel(dto);
                    return tokenizeCardUseCase.tokenize(card);
                })
                .doOnNext(token -> log.info("Tokenize generate successfully - ID: {}", token.getValue()))
                .flatMap(token -> ServerResponse.ok().bodyValue(token))
                .doOnError(error -> log.error("Generation failed: {}", error.getMessage()))
                .doOnSuccess(response -> log.info("Generation process completed"));
    }

    @Operation(summary = "Search products")
    public Mono<ServerResponse> searchProducts(ServerRequest serverRequest) {
        log.info("Starting search products process");

        String name = serverRequest.queryParam("name").orElse("");
        Flux<Product> productsFlux = searchProductsUseCase.searchByName(name)
                .doOnComplete(() -> log.info("Searching products process completed successfully."))
                .doOnError(error -> log.error("Search product failed: {}", error.getMessage()));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productsFlux, Product.class);
    }


    @Operation(summary = "Add a product to the shopping cart")
    public Mono<ServerResponse> addProductToCart(ServerRequest request) {
        String clientId = request.pathVariable("clientId");
        log.info("Starting process to add product to cart for client ID: {}", clientId);

        return request.bodyToMono(AddProductDTO.class)
                .flatMap(validationHandler::validate)
                .flatMap(dto -> addProductToCartUseCase.addProduct(clientId, dto.productId(), dto.quantity()))
                .flatMap(updatedCart -> ServerResponse.ok().bodyValue(updatedCart))
                .doOnError(error -> log.error("Error adding product to cart: {}", error.getMessage()))
                .doOnError(error -> log.error("Error adding product to cart for client {}: {}", clientId, error.getMessage()));
    }








}
