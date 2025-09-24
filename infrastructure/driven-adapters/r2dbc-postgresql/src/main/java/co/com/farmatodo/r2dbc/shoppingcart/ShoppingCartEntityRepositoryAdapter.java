package co.com.farmatodo.r2dbc.shoppingcart;

import co.com.farmatodo.model.cart.ShoppingCart;
import co.com.farmatodo.model.cart.gateways.ShoppingCartRepository;
import co.com.farmatodo.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class ShoppingCartEntityRepositoryAdapter
        extends ReactiveAdapterOperations<ShoppingCart, ShoppingCartEntity, String, ShoppingCartEntityRepository>
        implements ShoppingCartRepository {

    public ShoppingCartEntityRepositoryAdapter(ShoppingCartEntityRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, ShoppingCart.class));
    }

    /**
     * Finds a shopping cart by its client ID.
     * @param clientId The ID of the client.
     * @return A Mono emitting the ShoppingCart, or empty if not found.
     */
    @Override
    public Mono<ShoppingCart> findByClientId(String clientId) {
        log.info("Finding cart for client ID: {}", clientId);
        // The repository's findById method is used here, as clientId is the entity's ID.
        return this.repository.findById(clientId)
                .map(this::toEntity)
                .doOnSuccess(cart -> {
                    if (cart != null) {
                        log.info("Cart found for client ID: {}", cart.getClientId());
                    }
                });
    }

    /**
     * Saves or updates a shopping cart.
     * It first checks if the cart exists to set the 'isNew' flag for the Persistable entity,
     * ensuring an INSERT is performed for new carts and an UPDATE for existing ones.
     * @param cart The shopping cart domain model to save.
     * @return A Mono emitting the saved shopping cart.
     */
    @Override
    public Mono<ShoppingCart> save(ShoppingCart cart) {
        log.info("Attempting to save cart for client ID: {}", cart.getClientId());

        // Map the domain model to the data entity
        ShoppingCartEntity cartEntity = toData(cart);

        // Check if the entity already exists to set the 'isNew' flag
        return this.repository.existsById(cartEntity.getId())
                .flatMap(exists -> {
                    cartEntity.setNew(!exists);
                    log.info("Cart for client {} is new? {}", cartEntity.getId(), !exists);
                    return this.repository.save(cartEntity);
                })
                .map(this::toEntity)
                .doOnSuccess(savedCart -> log.info("Cart saved successfully for client ID: {}", savedCart.getClientId()))
                .doOnError(error -> log.error("Error saving cart for client ID {}: {}", cart.getClientId(), error.getMessage()));
    }
}


