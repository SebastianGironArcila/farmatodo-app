package co.com.farmatodo.api;

import co.com.farmatodo.api.config.ValidationHandler;
import co.com.farmatodo.api.dto.card.CardDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
            summary = "Ping endpoint",
            description = "Checks if the service is up and running.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is active", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    public Mono<ServerResponse> ping(ServerRequest serverRequest) {
        return ServerResponse.ok().bodyValue("pong");
    }

    @Operation(
            summary = "Register a new client",
            description = "Registers a new client in the system.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Client data to register",
                    content = @Content(schema = @Schema(implementation = CreateClientDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Client successfully registered", content = @Content(schema = @Schema(implementation = Client.class))),
                    @ApiResponse(responseCode = "400", description = "Validation or business error", content = @Content(schema = @Schema(example = "{\"error\": \"VALIDATION_FAILED\", \"message\": [\"email: Email is required\"]}"))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
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

    @Operation(
            summary = "Tokenize a credit card",
            description = "Tokenizes a credit card and returns the generated token.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Credit card data to tokenize",
                    content = @Content(schema = @Schema(implementation = CardDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token successfully generated", content = @Content(schema = @Schema(implementation = Token.class))),
                    @ApiResponse(responseCode = "400", description = "Validation or business error", content = @Content(schema = @Schema(example = "{\"error\": \"TOKENIZATION_REJECTED_BY_PROBABILITY\", \"message\": \"Tokenization rejected by probability\"}"))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    public Mono<ServerResponse> tokenizeCard(ServerRequest request) {
        log.info("Starting credit card tokenization process");
        return request.bodyToMono(CardDTO.class)
                .flatMap(validationHandler::validate)
                .flatMap(dto -> {
                    Card card = cardDTOMapper.toModel(dto);
                    return tokenizeCardUseCase.tokenize(card);
                })
                .doOnNext(token -> log.info("Token generated successfully - ID: {}", token.getValue()))
                .flatMap(token -> ServerResponse.ok().bodyValue(token))
                .doOnError(error -> log.error("Tokenization failed: {}", error.getMessage()))
                .doOnSuccess(response -> log.info("Tokenization process completed"));
    }

    @Operation(
            summary = "Search products",
            description = "Searches for products by name.",
            parameters = {
                    @Parameter(name = "name", description = "Product name to search for", required = false)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of found products",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))
                    ),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    public Mono<ServerResponse> searchProducts(ServerRequest serverRequest) {
        log.info("Starting product search process");
        String name = serverRequest.queryParam("name").orElse("");
        Flux<Product> productsFlux = searchProductsUseCase.searchByName(name)
                .doOnComplete(() -> log.info("Product search process completed successfully."))
                .doOnError(error -> log.error("Product search failed: {}", error.getMessage()));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productsFlux, Product.class);
    }

    @Operation(
            summary = "Add a product to the shopping cart",
            description = "Adds a product to a client's shopping cart.",
            parameters = {
                    @Parameter(name = "clientId", in = ParameterIn.PATH, description = "Client ID", required = true)
            },
            requestBody = @RequestBody(
                    required = true,
                    description = "Product data to add",
                    content = @Content(schema = @Schema(implementation = AddProductDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product successfully added to the cart", content = @Content(schema = @Schema(implementation = ShoppingCart.class))),
                    @ApiResponse(responseCode = "400", description = "Validation or business error", content = @Content(schema = @Schema(example = "{\"error\": \"INSUFFICIENT_STOCK\", \"message\": \"Insufficient Stock\"}"))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    public Mono<ServerResponse> addProductToCart(ServerRequest request) {
        Integer clientId = Integer.valueOf(request.pathVariable("clientId"));
        log.info("Starting process to add product to cart for client ID: {}", clientId);
        return request.bodyToMono(AddProductDTO.class)
                .flatMap(validationHandler::validate)
                .flatMap(dto -> addProductToCartUseCase.addProduct(clientId, dto.productId(), dto.quantity()))
                .flatMap(updatedCart -> ServerResponse.ok().bodyValue(updatedCart))
                .doOnError(error -> log.error("Error adding product to cart: {}", error.getMessage()))
                .doOnError(error -> log.error("Error adding product to cart for client {}: {}", clientId, error.getMessage()));
    }
}
