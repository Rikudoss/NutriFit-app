package kz.nutrifit.backend.auth.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Событие из топика user-events, публикуется auth-service-ом.
 * Используется и для USER_REGISTERED, и для USER_VERIFIED — eventType различает их.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) — толерантность к расширению схемы:
 * если auth-service добавит поле, монолит не упадёт при десериализации.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisteredEvent {
    private Long userId;
    private String email;
    private String eventType;
    private Instant timestamp;
}
