package kz.nutrifit.nutrition.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "nutrition-service")
    public void consume(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<>() {});
            String eventType = String.valueOf(payload.get("eventType"));
            Object rawUserId = payload.get("userId");

            log.info("Received user event: type={}, userId={}", eventType, rawUserId);

            // USER_REGISTERED: питание не нужно создавать заранее — создаётся по первому запросу
            // USER_DELETED: удаление meals/goals — реализуем в будущей задаче

        } catch (Exception ex) {
            log.error("Failed to process user event: {}", ex.getMessage(), ex);
        }
    }
}
