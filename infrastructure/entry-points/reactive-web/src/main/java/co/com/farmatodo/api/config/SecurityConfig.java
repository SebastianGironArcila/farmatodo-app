package co.com.farmatodo.api.config;

import co.com.farmatodo.api.security.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter(@Value("${security.api-key}") String apiKey) {
        return new ApiKeyAuthFilter(apiKey);
    }
}
