package co.com.farmatodo.r2dbc.client;

import co.com.farmatodo.model.client.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientEntityRepositoryAdapterTest {

    @InjectMocks
    private ClientEntityRepositoryAdapter adapter;

    @Mock
    private ClientEntityRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    private final ClientEntity entity = ClientEntity.builder()
            .id(1)
            .name("John Doe")
            .email("john@test.com")
            .phone("1234567890")
            .address("Street 123")
            .build();

    private final Client model = Client.builder()
            .id(1)
            .name("John Doe")
            .email("john@test.com")
            .phone("1234567890")
            .address("Street 123")
            .build();

    @Test
    void shouldSaveClientSuccessfully() {
        when(mapper.map(entity, Client.class)).thenReturn(model);
        when(mapper.map(model, ClientEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<Client> result = adapter.save(model);

        StepVerifier.create(result)
                .expectNext(model)
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenSaveFails() {
        when(mapper.map(model, ClientEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("DB error")));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(adapter.save(model))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }

    @Test
    void shouldFindClientByEmailSuccessfully() {
        when(repository.findByEmail("john@test.com")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Client.class)).thenReturn(model);

        StepVerifier.create(adapter.findByEmail("john@test.com"))
                .expectNextMatches(cli -> cli.getEmail().equals("john@test.com"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenClientNotFoundByEmail() {
        when(repository.findByEmail("notfound@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByEmail("notfound@test.com"))
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenFindByEmailFails() {
        when(repository.findByEmail("john@test.com"))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByEmail("john@test.com"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }

    @Test
    void shouldFindClientByPhoneSuccessfully() {
        when(repository.findByPhone("1234567890")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Client.class)).thenReturn(model);

        StepVerifier.create(adapter.findByPhone("1234567890"))
                .expectNextMatches(cli -> cli.getPhone().equals("1234567890"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenClientNotFoundByPhone() {
        when(repository.findByPhone("0000000000")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByPhone("0000000000"))
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenFindByPhoneFails() {
        when(repository.findByPhone("1234567890"))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByPhone("1234567890"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }
    @Test
    void shouldFindClientByIdSuccessfully() {
        when(repository.findById(1)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Client.class)).thenReturn(model);

        StepVerifier.create(adapter.findById(1))
                .expectNextMatches(cli -> cli.getId().equals(1)
                        && cli.getName().equals("John Doe")
                        && cli.getEmail().equals("john@test.com")
                        && cli.getPhone().equals("1234567890")
                        && cli.getAddress().equals("Street 123"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenClientNotFoundById() {
        when(repository.findById(2)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(2))
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenFindByIdFails() {
        when(repository.findById(1))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findById(1))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }
}
