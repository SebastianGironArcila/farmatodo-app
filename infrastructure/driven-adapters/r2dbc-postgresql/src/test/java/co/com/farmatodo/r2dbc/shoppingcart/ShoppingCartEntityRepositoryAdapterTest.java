package co.com.farmatodo.r2dbc.shoppingcart;

import co.com.farmatodo.model.cart.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartEntityRepositoryAdapterTest {

    @InjectMocks
    private ShoppingCartEntityRepositoryAdapter adapter;

    @Mock
    private ShoppingCartEntityRepository repository;

    @Mock
    private ObjectMapper mapper;

    private final ShoppingCartEntity entity = ShoppingCartEntity.builder()
            .clientId("client1")
            .isNew(true)
            .build();

    private final ShoppingCart model = ShoppingCart.builder()
            .clientId("client1")
            .build();



    @Test
    void shouldFindCartByClientId() {
        when(repository.findById("client1")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, ShoppingCart.class)).thenReturn(model);

        StepVerifier.create(adapter.findByClientId("client1"))
                .expectNext(model)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenCartNotFound() {
        when(repository.findById("client1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByClientId("client1"))
                .verifyComplete();
    }


    @Test
    void shouldSaveNewCart() {
        when(mapper.map(model, ShoppingCartEntity.class)).thenReturn(entity);
        when(repository.existsById("client1")).thenReturn(Mono.just(false));
        when(repository.save(any(ShoppingCartEntity.class))).thenReturn(Mono.just(entity));
        when(mapper.map(entity, ShoppingCart.class)).thenReturn(model);

        StepVerifier.create(adapter.save(model))
                .expectNext(model)
                .verifyComplete();

        verify(repository).save(entity);
    }

    @Test
    void shouldUpdateExistingCart() {
        when(mapper.map(model, ShoppingCartEntity.class)).thenReturn(entity);
        when(repository.existsById("client1")).thenReturn(Mono.just(true));
        when(repository.save(any(ShoppingCartEntity.class))).thenReturn(Mono.just(entity));
        when(mapper.map(entity, ShoppingCart.class)).thenReturn(model);

        StepVerifier.create(adapter.save(model))
                .expectNext(model)
                .verifyComplete();

        verify(repository).save(entity);
    }

    @Test
    void shouldPropagateErrorOnSave() {
        when(mapper.map(model, ShoppingCartEntity.class)).thenReturn(entity);
        when(repository.existsById("client1")).thenReturn(Mono.just(true));
        when(repository.save(any(ShoppingCartEntity.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(model))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }
}
