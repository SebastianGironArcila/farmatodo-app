package co.com.farmatodo.usecase.cart;

import co.com.farmatodo.model.cart.CartItem;
import co.com.farmatodo.model.cart.ShoppingCart;
import co.com.farmatodo.model.cart.gateways.ShoppingCartRepository;
import co.com.farmatodo.model.common.exception.BusinessException;
import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Optional;

@RequiredArgsConstructor
public class AddProductToCartUseCase {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductRepository productRepository;

    public Mono<ShoppingCart> addProduct(String clientId, String productId, int quantityToAdd) {
        // 1. Buscar el producto. Si no existe, emite error.
        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(BusinessException.Type.PRODUCT_NOT_FOUND.build()));

        // 2. Buscar el carrito. Si no existe, crea uno nuevo en memoria.
        Mono<ShoppingCart> cartMono = shoppingCartRepository.findByClientId(clientId)
                .defaultIfEmpty(ShoppingCart.builder()
                        .clientId(clientId)
                        .items(new HashMap<>())
                        .build());

        // 3. Combinar ambos Monos para ejecutar la lógica de negocio
        return Mono.zip(productMono, cartMono)
                .flatMap(tuple -> {
                    Product product = tuple.getT1();
                    ShoppingCart cart = tuple.getT2();
                    return addProductToCart(cart, product, quantityToAdd);
                })
                // 4. Guardar el carrito actualizado en el repositorio
                .flatMap(shoppingCartRepository::save);
    }

    private Mono<ShoppingCart> addProductToCart(ShoppingCart cart, Product product, int quantityToAdd) {
        // Obtenemos una copia mutable del mapa de items usando el accessor del record
        var mutableItems = new HashMap<>(cart.getItems());

        CartItem existingItem = mutableItems.get(product.getId());

        int quantityAlreadyInCart = Optional.ofNullable(existingItem)
                .map(CartItem::getQuantity) // Usamos el accessor del record
                .orElse(0);

        int newTotalQuantity = quantityAlreadyInCart + quantityToAdd;

        // Validamos el stock contra la cantidad total que habría en el carrito
        if (product.getStock() < newTotalQuantity) {
            // Devolvemos un error de forma reactiva, en lugar de lanzar una excepción
            return Mono.error(BusinessException.Type.INSUFFICIENT_STOCK.build());
        }

        CartItem newItem = CartItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(newTotalQuantity)
                .price(product.getPrice())
                .build();

        mutableItems.put(product.getId(), newItem);

        // Devolvemos un nuevo objeto ShoppingCart con el mapa de items actualizado, envuelto en un Mono
        return Mono.just(cart.toBuilder().items(mutableItems).build());
    }
}

