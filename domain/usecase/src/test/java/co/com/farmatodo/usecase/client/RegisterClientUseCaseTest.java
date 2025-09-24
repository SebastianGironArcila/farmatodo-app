package co.com.farmatodo.usecase.client;


import co.com.farmatodo.model.client.Client;
import co.com.farmatodo.model.client.gateways.ClientRepository;
import co.com.farmatodo.model.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class RegisterClientUseCaseTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private RegisterClientUseCase useCase;

    private Client client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = Client.builder()
                .email("test@example.com")
                .phone("123456789")
                .name("Juan Pérez")
                .build();
    }

    @Test
    void mustRegisterClientSuccessfully() {
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Mono.empty());
        when(clientRepository.findByPhone(client.getPhone())).thenReturn(Mono.empty());
        when(clientRepository.save(client)).thenReturn(Mono.just(client));

        StepVerifier.create(useCase.register(client))
                .expectNext(client)
                .verifyComplete();

        verify(clientRepository).save(client);
    }

    @Test
    void mustFailWhenEmailAlreadyRegistered() {
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Mono.just(client));

        StepVerifier.create(useCase.register(client))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getCode().equals(BusinessException.Type.EMAIL_ALREADY_REGISTERED.name()))
                .verify();
    }

    @Test
    void mustFailWhenPhoneAlreadyRegistered() {
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Mono.empty());
        when(clientRepository.findByPhone(client.getPhone())).thenReturn(Mono.just(client));

        StepVerifier.create(useCase.register(client))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getCode().equals(BusinessException.Type.PHONE_ALREADY_REGISTERED.name()))
                .verify();
    }
}
