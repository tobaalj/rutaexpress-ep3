package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Configuración del administrador (prefijo app.admin en application.yaml). */
@ConfigurationProperties(prefix = "app.admin")
public record RabbitAdminProperties(List<String> reservedPrefixes) {

    public RabbitAdminProperties {
        reservedPrefixes = reservedPrefixes == null ? List.of("amq.") : List.copyOf(reservedPrefixes);
    }
}
