package co.com.farmatodo.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/ping",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "ping"
            ),
            @RouterOperation(
                    path = "/api/v1/client",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "registerClient"
            ),
            @RouterOperation(
                    path = "/api/v1/tokens",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "tokenizeCard"
            ),
            @RouterOperation(
                    path = "/api/v1/products",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "searchProducts"
            ),
            @RouterOperation(
                    path = "/api/v1/cart/{clientId}/add",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "addProductToCart",
                    operation = @Operation(
                            summary = "Add a product to the shopping cart",
                            parameters = {
                                    @Parameter(
                                            name = "clientId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Client ID"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/ping"), handler::ping)
                .andRoute(POST("/api/v1/client"), handler::registerClient)
                .andRoute(POST("/api/v1/tokens"), handler::tokenizeCard)
                .andRoute(GET("/api/v1/products"), handler::searchProducts)
                .andRoute(POST("/api/v1/cart/{clientId}/add"), handler::addProductToCart);
    }
}
