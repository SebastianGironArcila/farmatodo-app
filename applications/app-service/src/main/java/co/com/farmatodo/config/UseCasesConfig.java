
package co.com.farmatodo.config;

import co.com.farmatodo.model.product.gateways.ProductRepository;
import co.com.farmatodo.model.searchhistory.gateways.SearchHistoryRepository;
import co.com.farmatodo.usecase.product.SearchProductsUseCase;
import co.com.farmatodo.usecase.token.TokenizeCardUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.farmatodo.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        TokenizeCardUseCase.class,
                        SearchProductsUseCase.class
                })
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    @Bean
    public TokenizeCardUseCase tokenizeCardUseCase(
            @Value("${tokenization.rejection-probability}") double rejectionProbability) {
        return new TokenizeCardUseCase(rejectionProbability);
    }

    @Bean
    public SearchProductsUseCase searchProductsUseCase(
            ProductRepository productRepository,
            SearchHistoryRepository searchHistoryRepository,
            @Value("${products.search.min-stock}") int minStock) {
        return new SearchProductsUseCase(productRepository, searchHistoryRepository,minStock);
    }
}

