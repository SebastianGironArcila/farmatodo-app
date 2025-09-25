package co.com.farmatodo.r2dbc.client;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ClientEntityRepository extends ReactiveCrudRepository<ClientEntity, Long>, ReactiveQueryByExampleExecutor<ClientEntity> {
    Mono<ClientEntity> findByEmail(String email);
    Mono<ClientEntity> findByPhone(String phone);
    Mono<ClientEntity> findById(Integer id);

}

