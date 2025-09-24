package co.com.farmatodo.r2dbc.searchhistory;

import co.com.farmatodo.model.searchhistory.SearchHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchHistoryEntityRepositoryAdapterTest {

    @InjectMocks
    private SearchHistoryEntityRepositoryAdapter adapter;

    @Mock
    private SearchHistoryEntityRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    private final SearchHistoryEntity entity = SearchHistoryEntity.builder()
            .id("log1")
            .searchTerm("aspirin")
            .searchTimestamp(LocalDateTime.now())
            .isNew(true)
            .build();

    private final SearchHistory model = SearchHistory.builder()
            .id("log1")
            .searchTerm("aspirin")
            .searchTimestamp(LocalDateTime.now())
            .build();

    @Test
    void shouldSaveSearchHistory() {
        // Arrange
        when(mapper.map(model, SearchHistoryEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, SearchHistory.class)).thenReturn(model);
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // devolver el mismo Mono

        // Act
        Mono<SearchHistory> result = adapter.save(model);

        // Assert
        StepVerifier.create(result)
                .expectNext(model)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenRepositoryFails() {
        // Arrange
        when(mapper.map(model, SearchHistoryEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("DB error")));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(adapter.save(model))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();
    }
}
