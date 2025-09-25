package co.com.farmatodo.usecase.cart;

import co.com.farmatodo.model.cart.CartItem;
import co.com.farmatodo.model.cart.ShoppingCart;
import co.com.farmatodo.model.cart.gateways.ShoppingCartRepository;
import co.com.farmatodo.model.client.Client;
import co.com.farmatodo.model.client.gateways.ClientRepository;
import co.com.farmatodo.model.common.exception.BusinessException;
import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddProductToCartUseCaseTest {

    private ShoppingCartRepository shoppingCartRepository;
    private ProductRepository productRepository;
    private ClientRepository clientRepository;

    private AddProductToCartUseCase useCase;

    private final Client client = Client.builder()
            .id(1)
            .name("Test")
            .email("test@test.com")
            .phone("123")
            .address("address")
            .build();

    @BeforeEach
    void setUp() {
        shoppingCartRepository = mock(ShoppingCartRepository.class);
        productRepository = mock(ProductRepository.class);
        clientRepository = mock(ClientRepository.class);
        useCase = new AddProductToCartUseCase(shoppingCartRepository, productRepository, clientRepository);
    }

    @Test
    void mustFailWhenProductNotFound() {
        Integer clientId = 1;
        String productId = "p1";

        when(clientRepository.findById(clientId)).thenReturn(Mono.just(client));
        when(productRepository.findById(productId)).thenReturn(Mono.empty());
        when(shoppingCartRepository.findByClientId(clientId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addProduct(clientId, productId, 1))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getCode().equals(BusinessException.Type.PRODUCT_NOT_FOUND.name()))
                .verify();

        verify(shoppingCartRepository).findByClientId(clientId);
        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void mustCreateNewCartWhenNoneExists() {
        Integer clientId = 1;
        String productId = "p1";

        when(clientRepository.findById(clientId)).thenReturn(Mono.just(client));

        Product product = Product.builder()
                .id(productId)
                .name("Aspirin")
                .stock(10)
                .price(BigDecimal.valueOf(100))
                .build();

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(shoppingCartRepository.findByClientId(clientId)).thenReturn(Mono.empty());
        when(shoppingCartRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.addProduct(clientId, productId, 2))
                .assertNext(cart -> {
                    assertThat(cart.getClientId()).isEqualTo(clientId);
                    assertThat(cart.getItems()).hasSize(1);
                    CartItem item = cart.getItems().get(productId);
                    assertThat(item.getQuantity()).isEqualTo(2);
                    assertThat(item.getProductName()).isEqualTo("Aspirin");
                })
                .verifyComplete();

        verify(shoppingCartRepository).save(any());
    }

    @Test
    void mustAddToExistingItemInCart() {
        Integer clientId = 1;
        String productId = "p1";

        when(clientRepository.findById(clientId)).thenReturn(Mono.just(client));

        Product product = Product.builder()
                .id(productId)
                .name("Aspirin")
                .stock(10)
                .price(BigDecimal.valueOf(50))
                .build();

        CartItem existingItem = CartItem.builder()
                .productId(productId)
                .productName("Aspirin")
                .quantity(3)
                .price(BigDecimal.valueOf(50))
                .build();

        ShoppingCart existingCart = ShoppingCart.builder()
                .clientId(clientId)
                .items(new HashMap<>() {{
                    put(productId, existingItem);
                }})
                .build();

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(shoppingCartRepository.findByClientId(clientId)).thenReturn(Mono.just(existingCart));
        when(shoppingCartRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.addProduct(clientId, productId, 2))
                .assertNext(cart -> {
                    CartItem item = cart.getItems().get(productId);
                    assertThat(item.getQuantity()).isEqualTo(5); // 3 existentes + 2 nuevos
                })
                .verifyComplete();

        verify(shoppingCartRepository).save(any());
    }

    @Test
    void mustFailWhenInsufficientStock() {
        Integer clientId = 1;
        String productId = "p1";

        when(clientRepository.findById(clientId)).thenReturn(Mono.just(client));

        Product product = Product.builder()
                .id(productId)
                .name("Ibuprofen")
                .stock(3)
                .price(BigDecimal.valueOf(80))
                .build();

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(shoppingCartRepository.findByClientId(clientId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addProduct(clientId, productId, 5))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getCode().equals(BusinessException.Type.INSUFFICIENT_STOCK.name()))
                .verify();

        verify(shoppingCartRepository, never()).save(any());
    }


}
