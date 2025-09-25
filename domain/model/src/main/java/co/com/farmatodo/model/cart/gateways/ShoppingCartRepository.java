package co.com.farmatodo.model.cart.gateways;


import co.com.farmatodo.model.cart.ShoppingCart;
import reactor.core.publisher.Mono;


public interface ShoppingCartRepository {


    /**
     * Busca un carrito de compras por el ID del cliente.
     * Si el carrito no existe, debería emitir un Mono vacío (Mono.empty()).
     * @param clientId El ID del cliente.
     * @return Un Mono que emite el ShoppingCart encontrado.
     */
    Mono<ShoppingCart> findByClientId(Integer clientId);


    /**
     * Guarda o actualiza un carrito de compras.
     * @param cart El carrito a guardar.
     * @return Un Mono que emite el carrito guardado.
     */
    Mono<ShoppingCart> save(ShoppingCart cart);
}



