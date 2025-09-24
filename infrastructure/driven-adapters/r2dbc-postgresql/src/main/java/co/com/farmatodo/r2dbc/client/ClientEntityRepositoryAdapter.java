package co.com.farmatodo.r2dbc.client;

import co.com.farmatodo.model.client.Client;
import co.com.farmatodo.model.client.gateways.ClientRepository;
import co.com.farmatodo.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class ClientEntityRepositoryAdapter extends ReactiveAdapterOperations<
        Client,
        ClientEntity,
        Long,
        ClientEntityRepository
        > implements ClientRepository {

    private final ClientEntityRepository repository;
    private final TransactionalOperator transactionalOperator;

    public ClientEntityRepositoryAdapter(ClientEntityRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, Client.class));
        this.repository = repository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Client> save(Client client) {
        return super.save(client)
                .as(transactionalOperator::transactional)
                .doOnSuccess(savedUser -> log.info("Client saved successfully - ID: {}", savedUser.getEmail()))
                .doOnError(error -> log.error("Error saving client: {}", error.getMessage()));
    }

    @Override
    public Mono<Client> findByEmail(String email) {
        log.info("Finding client by email: {}", email);
        return repository.findByEmail(email)
                .map(this::toEntity)
                .doOnNext(cli -> log.info("Client found: {}", cli.getEmail()))
                .doOnError(error -> log.error("Error finding client by email: {}", error.getMessage()));
    }

    @Override
    public Mono<Client> findByPhone(String phone) {
        log.info("Finding client by phone: {}", phone);
        return repository.findByPhone(phone)
                .map(this::toEntity)
                .doOnNext(cli -> log.info("Client found: {}", cli.getName()))
                .doOnError(error -> log.error("Error finding client by phone: {}", error.getMessage()));
    }
}
