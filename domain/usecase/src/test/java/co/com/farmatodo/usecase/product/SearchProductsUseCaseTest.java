package co.com.farmatodo.usecase.product;

import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.model.product.gateways.ProductRepository;
import co.com.farmatodo.model.searchhistory.SearchHistory;
import co.com.farmatodo.model.searchhistory.gateways.SearchHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchProductsUseCaseTest {

    private ProductRepository productRepository;
    private SearchHistoryRepository searchHistoryRepository;
    private SearchProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        searchHistoryRepository = mock(SearchHistoryRepository.class);
        useCase = new SearchProductsUseCase(productRepository, searchHistoryRepository, 5);
    }

    @Test
    void mustReturnProductsAndSaveHistory() {
        String searchTerm = "aspirin";
        Product product1 = Product.builder().id("1").name("aspirin 100mg").stock(10).build();
        Product product2 = Product.builder().id("2").name("aspirin forte").stock(20).build();

        when(productRepository.findByNameContainingAndStockGreaterThan(searchTerm, 5))
                .thenReturn(Flux.just(product1, product2));
        when(searchHistoryRepository.save(any(SearchHistory.class)))
                .thenReturn(Mono.just(new SearchHistory()));

        StepVerifier.create(useCase.searchByName(searchTerm))
                .expectNext(product1, product2)
                .verifyComplete();

        // Capturamos el SearchHistory que se guardó
        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository).save(captor.capture());

        SearchHistory savedHistory = captor.getValue();
        assertThat(savedHistory.getSearchTerm()).isEqualTo(searchTerm);
        assertThat(savedHistory.getSearchTimestamp()).isNotNull();
    }

    @Test
    void mustReturnEmptyIfNoProductsFound() {
        String searchTerm = "notfound";

        when(productRepository.findByNameContainingAndStockGreaterThan(searchTerm, 5))
                .thenReturn(Flux.empty());
        when(searchHistoryRepository.save(any(SearchHistory.class)))
                .thenReturn(Mono.just(new SearchHistory()));

        StepVerifier.create(useCase.searchByName(searchTerm))
                .verifyComplete();

        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void mustStillReturnProductsIfHistorySaveFails() {
        String searchTerm = "ibuprofen";
        Product product = Product.builder().id("3").name("ibuprofen 200mg").stock(15).build();

        when(productRepository.findByNameContainingAndStockGreaterThan(searchTerm, 5))
                .thenReturn(Flux.just(product));
        when(searchHistoryRepository.save(any(SearchHistory.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.searchByName(searchTerm))
                .expectNext(product)
                .verifyComplete();

        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }
}
