package co.com.farmatodo.usecase.product;


import co.com.farmatodo.model.product.Product;
import co.com.farmatodo.model.product.gateways.ProductRepository;
import co.com.farmatodo.model.searchhistory.SearchHistory;
import co.com.farmatodo.model.searchhistory.gateways.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class SearchProductsUseCase {


    private final ProductRepository productRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final int minStock;


    public Flux<Product> searchByName(String name) {
        // 1. Define la operación de guardado como un Mono<Void> para que no nos importe su resultado.
        Mono<Void> saveOperation = Mono.defer(() -> {
                    SearchHistory history = new SearchHistory(
                            UUID.randomUUID().toString(),
                            name,
                            LocalDateTime.now()
                    );
                    // El save() devuelve un Mono<SearchHistory>, lo convertimos a Mono<Void> con then()
                    return searchHistoryRepository.save(history).then();
                })
                .subscribeOn(Schedulers.boundedElastic()) // Ejecutar en otro hilo
                .onErrorComplete(); // Si falla el guardado, no rompas la cadena principal.


        // 2. El flujo principal
        Flux<Product> productsFlux = productRepository.findByNameContainingAndStockGreaterThan(name, minStock);


        // 3. Composición correcta:
        //    El operador 'and' se suscribe a ambos flujos. Espera a que 'saveOperation' complete
        //    y luego deja pasar los elementos de 'productsFlux'.
        return Mono.when(saveOperation).thenMany(productsFlux);
    }
}



