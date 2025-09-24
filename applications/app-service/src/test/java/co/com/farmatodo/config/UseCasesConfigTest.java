package co.com.farmatodo.config;

import co.com.farmatodo.model.cart.gateways.ShoppingCartRepository;
import co.com.farmatodo.model.client.gateways.ClientRepository;
import co.com.farmatodo.model.product.gateways.ProductRepository;
import co.com.farmatodo.model.searchhistory.gateways.SearchHistoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
            Properties properties = new Properties();
            properties.setProperty("tokenization.rejection-probability", "0.1");
            properties.setProperty("products.search.min-stock", "2");

            configurer.setProperties(properties);
            return configurer;
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }

        @Bean
        public ClientRepository clientRepository() {
            return Mockito.mock(ClientRepository.class);
        }

        @Bean
        public ProductRepository productRepository() {
            return Mockito.mock(ProductRepository.class);
        }

        @Bean
        public SearchHistoryRepository searchHistoryRepository() {
            return Mockito.mock(SearchHistoryRepository.class);
        }


        @Bean
        public ShoppingCartRepository shoppingCartRepository() {
            return Mockito.mock(ShoppingCartRepository.class);
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}


