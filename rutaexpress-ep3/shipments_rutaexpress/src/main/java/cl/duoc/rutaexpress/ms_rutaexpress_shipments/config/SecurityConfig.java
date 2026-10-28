package cl.duoc.rutaexpress.ms_rutaexpress_shipments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitado para APIs stateless
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Necesario para la consola H2
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll() // Permitir consola H2 en desarrollo
                .requestMatchers("/api/shipments/**").authenticated() // Proteger endpoints con JWT
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {})); // Valida tokens JWT

        return http.build();
    }
}