package co.com.farmatodo.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class ApiKeyAuthFilter implements WebFilter {

    private final String configuredApiKey;
    private final PathPattern pattern;

    public ApiKeyAuthFilter(@Value("${security.api-key}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
        this.pattern = new PathPatternParser().parse("/api/v1/**");
    }

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (pattern.matches(exchange.getRequest().getPath().pathWithinApplication())) {
            String apiKeyHeader = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

            if (configuredApiKey.equals(apiKeyHeader)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }
}
