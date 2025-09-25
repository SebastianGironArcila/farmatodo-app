package co.com.farmatodo.r2dbc.shoppingcart;

import co.com.farmatodo.model.cart.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Mock
    private TransactionalOperator transactionalOperator;

    private final ShoppingCartEntity entity = ShoppingCartEntity.builder()
            .clientId(1)
            .items("{}")
            .isNew(true)
            .build();

    private final ShoppingCart model = ShoppingCart.builder()
            .clientId(1)
            .items(new HashMap<>())
            .build();

    @Test
    void shouldFindCartByClientId() {
        when(repository.findById(1)).thenReturn(Mono.just(entity));
        // No es necesario mockear mapper.map aquí, el adapter usa su propio mapeo

        StepVerifier.create(adapter.findByClientId(1))
                .assertNext(cart -> {
                    assertEquals(1, cart.getClientId());
                    assertEquals(new HashMap<>(), cart.getItems());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenCartNotFound() {
        when(repository.findById(1)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByClientId(1))
                .verifyComplete();
    }

    @Test
    void shouldSaveNewCart() {
        when(repository.existsById(1)).thenReturn(Mono.just(false));
        when(repository.save(any(ShoppingCartEntity.class))).thenReturn(Mono.just(entity));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(adapter.save(model))
                .assertNext(cart -> {
                    assertEquals(1, cart.getClientId());
                    assertEquals(new HashMap<>(), cart.getItems());
                })
                .verifyComplete();

        ArgumentCaptor<ShoppingCartEntity> captor = ArgumentCaptor.forClass(ShoppingCartEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("{}", captor.getValue().getItems());
    }

    @Test
    void shouldUpdateExistingCart() {
        ShoppingCartEntity updatedEntity = ShoppingCartEntity.builder()
                .clientId(1)
                .items("{}")
                .isNew(false)
                .build();

        when(repository.existsById(1)).thenReturn(Mono.just(true));
        when(repository.save(any(ShoppingCartEntity.class))).thenReturn(Mono.just(updatedEntity));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(adapter.save(model))
                .assertNext(cart -> {
                    assertEquals(1, cart.getClientId());
                    assertEquals(new HashMap<>(), cart.getItems());
                })
                .verifyComplete();

        ArgumentCaptor<ShoppingCartEntity> captor = ArgumentCaptor.forClass(ShoppingCartEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("{}", captor.getValue().getItems());
        assertEquals(false, captor.getValue().isNew());
    }

    @Test
    void shouldPropagateErrorOnSave() {
        when(repository.existsById(1)).thenReturn(Mono.just(true));
        when(repository.save(any(ShoppingCartEntity.class))).thenReturn(Mono.error(new RuntimeException("DB error")));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(adapter.save(model))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }
}
