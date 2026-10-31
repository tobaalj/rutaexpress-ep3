package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void emptyQueueNameIsInvalid() {
        QueueRequestDTO dto = new QueueRequestDTO();
        dto.setName("");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void queueNameWithSpacesIsInvalid() {
        QueueRequestDTO dto = new QueueRequestDTO();
        dto.setName("mi cola");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void zeroTtlIsInvalid() {
        QueueRequestDTO dto = new QueueRequestDTO();
        dto.setName("q1");
        dto.setMessageTtlMs(0L);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void validQueueIsAccepted() {
        QueueRequestDTO dto = new QueueRequestDTO();
        dto.setName("rutaexpress.demo.queue");
        dto.setMessageTtlMs(1000L);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void exchangeRequiresType() {
        ExchangeRequestDTO dto = new ExchangeRequestDTO();
        dto.setName("ex1");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void bindingRequiresQueueAndExchange() {
        assertFalse(validator.validate(new BindingRequestDTO()).isEmpty());
    }
}
