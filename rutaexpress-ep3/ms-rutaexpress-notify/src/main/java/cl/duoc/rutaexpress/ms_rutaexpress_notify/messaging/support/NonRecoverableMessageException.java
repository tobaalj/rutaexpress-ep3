package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support;

/** Error que no se arregla reintentando (datos inválidos): el mensaje va directo a la DLQ. */
public class NonRecoverableMessageException extends RuntimeException {
    public NonRecoverableMessageException(String message) {
        super(message);
    }
}
