package co.com.farmatodo.r2dbc.searchhistory;

import co.com.farmatodo.model.searchhistory.SearchHistory;
import co.com.farmatodo.model.searchhistory.gateways.SearchHistoryRepository;
import co.com.farmatodo.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;


@Repository
@Slf4j
public class SearchHistoryEntityRepositoryAdapter extends ReactiveAdapterOperations<
        SearchHistory,
        SearchHistoryEntity,
        String,
        SearchHistoryEntityRepository
        > implements SearchHistoryRepository {


    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper mapper;


    public SearchHistoryEntityRepositoryAdapter(SearchHistoryEntityRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, SearchHistory.class));
        this.transactionalOperator = transactionalOperator;
        this.mapper = mapper;
    }


    @Override
    public Mono<SearchHistory> save(SearchHistory searchHistory) {
        SearchHistoryEntity entity = this.mapper.map(searchHistory, SearchHistoryEntity.class);
        entity.setNew(true);
        return repository.save(entity)
                .map(saveEntity -> this.mapper.map(saveEntity,SearchHistory.class))
                .as(transactionalOperator::transactional)
                .doOnSuccess(saved -> log.info("Log saved successfully - ID: {}", saved.getId()))
                .doOnError(error -> log.error("Error saving log: {}", error.getMessage()));
    }
}
