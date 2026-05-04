package kz.nutrifit.user.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.nutrifit.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "user-service")
    public void consume(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<>() {});
            String eventType = String.valueOf(payload.get("eventType"));

            if (!"USER_REGISTERED".equals(eventType)) return;

            Object rawUserId = payload.get("userId");
            if (rawUserId == null) {
                log.warn("USER_REGISTERED event missing userId");
                return;
            }

            Long userId = rawUserId instanceof Number n ? n.longValue() : Long.parseLong(rawUserId.toString());
            profileService.getOrCreate(userId);
            log.info("Profile created for userId={}", userId);

        } catch (Exception ex) {
            log.error("Failed to process user event: {}", ex.getMessage(), ex);
        }
    }
}
