package co.com.farmatodo.r2dbc.product;


import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductEntityRepository extends ReactiveCrudRepository<ProductEntity, String>, ReactiveQueryByExampleExecutor<ProductEntity> {
    @Query("SELECT * FROM product WHERE LOWER(name) LIKE LOWER(:name) AND stock > :minStock")
    Flux<ProductEntity> findByNameContainingAndStockGreaterThan(String name, int minStock);
}