package co.com.farmatodo.r2dbc.product;

import co.com.farmatodo.model.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntityRepositoryAdapterTest {

    @InjectMocks
    private ProductEntityRepositoryAdapter adapter;

    @Mock
    private ProductEntityRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    private final ProductEntity entity = ProductEntity.builder()
            .id("p1")
            .name("Aspirin")
            .stock(50)
            .price(BigDecimal.valueOf(20.0))
            .build();

    private final Product model = Product.builder()
            .id("p1")
            .name("Aspirin")
            .stock(50)
            .price(BigDecimal.valueOf(20.0))
            .build();

    @Test
    void shouldFindProductsByNameAndStockSuccessfully() {
        when(repository.findByNameContainingAndStockGreaterThan("%Aspirin%", 10))
                .thenReturn(Flux.just(entity));
        when(mapper.map(entity, Product.class)).thenReturn(model);

        StepVerifier.create(adapter.findByNameContainingAndStockGreaterThan("Aspirin", 10))
                .expectNextMatches(p -> p.getName().equals("Aspirin") && p.getStock() == 50)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNoProductsMatchSearch() {
        when(repository.findByNameContainingAndStockGreaterThan("%Unknown%", 10))
                .thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByNameContainingAndStockGreaterThan("Unknown", 10))
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenFindByNameFails() {
        when(repository.findByNameContainingAndStockGreaterThan("%Aspirin%", 10))
                .thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByNameContainingAndStockGreaterThan("Aspirin", 10))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }

    @Test
    void shouldFindProductByIdSuccessfully() {
        when(repository.findById("p1")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Product.class)).thenReturn(model);

        StepVerifier.create(adapter.findById("p1"))
                .expectNextMatches(p -> p.getId().equals("p1") && p.getName().equals("Aspirin"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenProductNotFoundById() {
        when(repository.findById("notfound")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("notfound"))
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenFindByIdFails() {
        when(repository.findById(anyString()))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findById("p1"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }
}
