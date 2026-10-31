package cl.duoc.rutaexpress.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${services.shipments.url}")
    private String shipmentsServiceUrl;

    @Value("${services.catalog.url}")
    private String catalogServiceUrl;

    @Bean
    public RestClient shipmentsRestClient() {
        return RestClient.builder()
                .baseUrl(shipmentsServiceUrl)
                .requestInterceptor(bearerTokenInterceptor())
                .build();
    }

    @Bean
    public RestClient catalogRestClient() {
        return RestClient.builder()
                .baseUrl(catalogServiceUrl)
                .requestInterceptor(bearerTokenInterceptor())
                .build();
    }

    /** Reenvía a los microservicios el JWT del IDaaS que llegó al BFF. */
    private ClientHttpRequestInterceptor bearerTokenInterceptor() {
        return (request, body, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
            }
            return execution.execute(request, body);
        };
    }
}
