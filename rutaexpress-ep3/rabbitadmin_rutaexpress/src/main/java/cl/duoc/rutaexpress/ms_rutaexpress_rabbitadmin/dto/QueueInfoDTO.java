package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto;

/** Estado de una cola. */
public record QueueInfoDTO(String name, boolean durable, int messageCount, int consumerCount) {}
