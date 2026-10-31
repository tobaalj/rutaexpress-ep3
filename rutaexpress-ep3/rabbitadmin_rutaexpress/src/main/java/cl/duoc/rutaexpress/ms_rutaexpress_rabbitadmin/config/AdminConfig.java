package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitAdminProperties.class)
public class AdminConfig {
    // AmqpAdmin (RabbitAdmin) lo provee la autoconfiguración de spring-boot-starter-amqp.
}
