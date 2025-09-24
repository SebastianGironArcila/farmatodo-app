package co.com.farmatodo.model.client.gateways;

import co.com.farmatodo.model.client.Client;
import reactor.core.publisher.Mono;

public interface ClientRepository {
    Mono<Client> save(Client client);
    Mono<Client> findByEmail(String email);
    Mono<Client> findByPhone(String phone);
}
