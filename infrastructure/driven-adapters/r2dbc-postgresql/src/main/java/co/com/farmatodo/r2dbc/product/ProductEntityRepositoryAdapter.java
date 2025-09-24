package co.com.farmatodo.r2dbc.product;

import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.model.product.gateways.ProductRepository;
import co.com.farmatodo.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class ProductEntityRepositoryAdapter extends ReactiveAdapterOperations<
        Product,
        ProductEntity,
        String,
        ProductEntityRepository
        > implements ProductRepository {

    private final ProductEntityRepository repository;
    private final TransactionalOperator transactionalOperator;

    public ProductEntityRepositoryAdapter(ProductEntityRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, Product.class));
        this.repository = repository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Flux<Product> findByNameContainingAndStockGreaterThan(String name, int minStock) {
        // Añadimos '%' para que funcione como un 'LIKE'
        String searchName = "%" + name + "%";
        return repository.findByNameContainingAndStockGreaterThan(searchName, minStock)
                .map(productEntity -> mapper.map(productEntity, Product.class))
                .doOnComplete(() -> log.info("Finished searching"))
                .doOnError(error -> log.error("Error finding product: {}", error.getMessage()));


    }

    @Override
    public Mono<Product> findById(String id) {
        return repository.findById(id)
                .map(entity -> mapper.map(entity, Product.class))
                .doOnNext(product -> log.info("Found Product - ID: {}", product.getName()))
                .doOnError(error -> log.error("Error finding product ID {}: {}", id, error.getMessage()));
    }



}
