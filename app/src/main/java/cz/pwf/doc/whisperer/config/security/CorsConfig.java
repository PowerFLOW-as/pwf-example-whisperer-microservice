package cz.pwf.whisperer.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Třída umožňuje konfiguračně definovat adresy URL, kterým bude umožněn přístup k endpointům při aktivování CORS.
 */
@Configuration
@ConditionalOnProperty(
        value="security.cors.enabled",
        havingValue = "true")
public class CorsConfig implements WebMvcConfigurer {

    @Value("${security.cors.allowed-origins:*}")
    private String[] corsAllowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsAllowedOrigins)
                .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(), HttpMethod.HEAD.name());
    }
}
