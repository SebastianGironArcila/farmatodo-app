package co.com.farmatodo.usecase.client;

import co.com.farmatodo.model.client.Client;
import co.com.farmatodo.model.client.gateways.ClientRepository;
import co.com.farmatodo.model.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterClientUseCase {

    private final ClientRepository clientRepository;

    public Mono<Client> register(Client client) {
        return clientRepository.findByEmail(client.getEmail())
                .flatMap(existing -> Mono.<Client>error(BusinessException.Type.EMAIL_ALREADY_REGISTERED.build()))
                .switchIfEmpty(Mono.defer(() ->
                        clientRepository.findByPhone(client.getPhone())
                                .flatMap(existing -> Mono.error(BusinessException.Type.PHONE_ALREADY_REGISTERED.build()))
                ))
                .switchIfEmpty(Mono.defer(() -> clientRepository.save(client)));
    }
}
