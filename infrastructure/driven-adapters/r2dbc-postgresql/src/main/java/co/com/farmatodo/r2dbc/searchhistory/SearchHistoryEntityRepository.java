package co.com.farmatodo.r2dbc.searchhistory;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SearchHistoryEntityRepository extends ReactiveCrudRepository<SearchHistoryEntity, String>, ReactiveQueryByExampleExecutor<SearchHistoryEntity> {

}
