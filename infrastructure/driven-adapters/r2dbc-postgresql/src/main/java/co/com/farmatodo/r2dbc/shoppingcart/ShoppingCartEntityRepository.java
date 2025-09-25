package co.com.farmatodo.r2dbc.shoppingcart;


import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ShoppingCartEntityRepository extends ReactiveCrudRepository<ShoppingCartEntity, Integer>, ReactiveQueryByExampleExecutor<ShoppingCartEntity> {
    Mono<ShoppingCartEntity> findById(Integer clientId);
}


