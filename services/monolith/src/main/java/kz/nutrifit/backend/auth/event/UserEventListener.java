package kz.nutrifit.backend.auth.event;

import kz.nutrifit.backend.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Слушает user-events из auth-service и синхронизирует таблицу users монолита.
 *
 * Зачем: монолит держит свою таблицу users (FK из profiles/meals/workouts/metrics).
 * Auth-service — источник правды, его id попадают в JWT и Gateway-ом пробрасываются
 * как X-User-Id. Чтобы FK в монолите находили строку, нужна копия.
 *
 * Идемпотентность: existsById + ON CONFLICT (id) DO NOTHING — двойная защита от
 * повторной доставки события (Kafka at-least-once).
 *
 * Native INSERT через JdbcTemplate, потому что монолитный User имеет
 * @GeneratedValue(IDENTITY): JPA save() с явно установленным id игнорирует id
 * и просит SERIAL подставить новый. Native SQL даёт явный контроль над id.
 *
 * После insert сдвигаем sequence: монолит пока имеет свой AuthService.register()
 * (он будет удалён в C.2), который дальше создаёт юзеров через IDENTITY. Без
 * setval следующий локальный register получит id, уже занятый из auth-service.
 */
@Component
@Slf4j
public class UserEventListener {

    private static final String EXTERNAL_PASSWORD_PLACEHOLDER = "EXTERNAL_AUTH";
    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public UserEventListener(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics = "user-events", groupId = "monolith-user-sync")
    public void handleUserEvent(UserRegisteredEvent event) {
        try {
            if (event == null || event.getUserId() == null || event.getEventType() == null) {
                log.warn("Received malformed user event: {}", event);
                return;
            }
            switch (event.getEventType()) {
                case "USER_REGISTERED" -> upsertUser(event);
                case "USER_VERIFIED" -> handleUserVerified(event);
                default -> log.debug("Ignoring user event with unknown type: {}", event.getEventType());
            }
        } catch (Exception ex) {
            // Не пробрасываем — Kafka не должен накапливать backpressure из-за бага в обработке
            log.error("Failed to process user event {}: {}", event, ex.getMessage(), ex);
        }
    }

    private void upsertUser(UserRegisteredEvent event) {
        if (userRepository.existsById(event.getUserId())) {
            log.debug("User id={} already synced — skipping USER_REGISTERED", event.getUserId());
            return;
        }

        int rows = jdbcTemplate.update(
                "INSERT INTO users (id, email, password, role) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO NOTHING",
                event.getUserId(),
                event.getEmail(),
                EXTERNAL_PASSWORD_PLACEHOLDER,
                DEFAULT_ROLE);

        if (rows > 0) {
            // Удерживаем последовательность впереди, пока в монолите остаётся локальный register (до C.2)
            jdbcTemplate.execute(
                    "SELECT setval(pg_get_serial_sequence('users', 'id'), " +
                            "GREATEST((SELECT COALESCE(MAX(id), 0) FROM users), 1))");
            log.info("Synced user id={} email={} from auth-service", event.getUserId(), event.getEmail());
        } else {
            log.debug("User id={} insert hit ON CONFLICT — already present", event.getUserId());
        }
    }

    private void handleUserVerified(UserRegisteredEvent event) {
        // Монолитная таблица users не имеет колонки status — поэтому verify пока no-op.
        // В C.2 либо удалим таблицу, либо добавим колонку. Логируем для трейсинга.
        boolean exists = userRepository.existsById(event.getUserId());
        log.info("USER_VERIFIED received for id={} (exists in monolith: {}); status sync is no-op until schema adds column",
                event.getUserId(), exists);
    }
}
