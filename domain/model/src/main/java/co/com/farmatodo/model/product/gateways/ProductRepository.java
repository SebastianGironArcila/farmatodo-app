package co.com.farmatodo.model.product.gateways;

import co.com.farmatodo.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Flux<Product> findByNameContainingAndStockGreaterThan(String name, int minStock);
    Mono<Product> findById(String id);
}
