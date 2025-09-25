package co.com.farmatodo.r2dbc.shoppingcart;

import co.com.farmatodo.model.cart.CartItem;
import co.com.farmatodo.model.cart.ShoppingCart;
import co.com.farmatodo.model.cart.gateways.ShoppingCartRepository;
import co.com.farmatodo.r2dbc.helper.ReactiveAdapterOperations;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class ShoppingCartEntityRepositoryAdapter
        extends ReactiveAdapterOperations<ShoppingCart, ShoppingCartEntity, Integer, ShoppingCartEntityRepository>
        implements ShoppingCartRepository {

    private final ObjectMapper jacksonMapper = new ObjectMapper();
    private final TransactionalOperator transactionalOperator;

    public ShoppingCartEntityRepositoryAdapter(
            ShoppingCartEntityRepository repository,
            org.reactivecommons.utils.ObjectMapper mapper,
            TransactionalOperator transactionalOperator
    ) {
        super(repository, mapper, entity -> {
            try {
                Map<String, CartItem> items = new HashMap<>();
                if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                    items = new ObjectMapper().readValue(entity.getItems(), new TypeReference<Map<String, CartItem>>() {});
                }
                return ShoppingCart.builder()
                        .clientId(entity.getClientId())
                        .items(items)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Error deserializando items del carrito", e);
            }
        });
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    protected ShoppingCartEntity toData(ShoppingCart cart) {
        try {
            String itemsJson = jacksonMapper.writeValueAsString(cart.getItems());
            return ShoppingCartEntity.builder()
                    .clientId(cart.getClientId())
                    .items(itemsJson)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error serializando items del carrito", e);
        }
    }

    @Override
    public Mono<ShoppingCart> findByClientId(Integer clientId) {
        log.info("Finding cart for client ID: {}", clientId);
        return this.repository.findById(clientId)
                .map(this::toEntity)
                .doOnSuccess(cart -> {
                    if (cart != null) {
                        log.info("Cart found for client ID: {}", cart.getClientId());
                    }
                });
    }

    @Override
    public Mono<ShoppingCart> save(ShoppingCart cart) {
        log.info("Attempting to save cart for client ID: {}", cart.getClientId());
        ShoppingCartEntity cartEntity = toData(cart);
        return this.repository.existsById(cartEntity.getClientId())
                .flatMap(exists -> {
                    cartEntity.setNew(!exists);
                    log.info("Cart for client {} is new? {}", cartEntity.getClientId(), !exists);
                    return this.repository.save(cartEntity);
                })
                .map(this::toEntity)
                .as(transactionalOperator::transactional)
                .doOnSuccess(savedCart -> log.info("Cart saved successfully for client ID: {}", savedCart.getClientId()))
                .doOnError(error -> log.error("Error saving cart for client ID {}: {}", cart.getClientId(), error.getMessage()));
    }
}
