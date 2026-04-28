package kz.nutrifit.auth.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class UserEventPublisher {

    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(Long userId, String email) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(userId)
                .email(email)
                .timestamp(Instant.now())
                .build();
        publish(userId, event, "user.registered");
    }

    public void publishUserVerified(Long userId, String email) {
        UserVerifiedEvent event = UserVerifiedEvent.builder()
                .userId(userId)
                .email(email)
                .timestamp(Instant.now())
                .build();
        publish(userId, event, "user.verified");
    }

    private void publish(Long userId, Object payload, String label) {
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(userId), payload);
            log.info("Published {} for userId={}", label, userId);
        } catch (Exception ex) {
            log.error("Failed to publish {} for userId={}: {}", label, userId, ex.getMessage(), ex);
        }
    }
}
