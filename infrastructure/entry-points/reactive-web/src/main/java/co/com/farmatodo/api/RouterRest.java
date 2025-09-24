package co.com.farmatodo.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/ping"), handler::ping)
                .andRoute(POST("/api/v1/client"), handler::registerClient)
                .andRoute(POST("/api/v1/tokens"), handler::tokenizeCard)
                .andRoute(GET("/api/v1/products"), handler::searchProducts)
                .andRoute(POST("/api/v1/cart/{clientId}/add"), handler::addProductToCart);



    }
}
