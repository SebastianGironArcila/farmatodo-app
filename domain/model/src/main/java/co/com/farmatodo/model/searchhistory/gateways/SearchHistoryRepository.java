package co.com.farmatodo.model.searchhistory.gateways;

import co.com.farmatodo.model.searchhistory.SearchHistory;
import reactor.core.publisher.Mono;

public interface SearchHistoryRepository {
    Mono<SearchHistory> save(SearchHistory searchHistory);
}
