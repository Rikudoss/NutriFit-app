package kz.nutrifit.backend.metrics;

import jakarta.validation.Valid;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;
    private final UserRepository userRepository;

    public MetricsController(MetricsService metricsService, UserRepository userRepository) {
        this.metricsService = metricsService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<HealthMetric> createMetric(Authentication authentication,
                                                     @RequestBody @Valid HealthMetricRequest request) {
        User user = userRef(authentication);
        return ResponseEntity.ok(metricsService.createMetric(request, user));
    }

    @GetMapping
    public ResponseEntity<List<HealthMetric>> getMetrics(Authentication authentication) {
        return ResponseEntity.ok(metricsService.getMetrics(userRef(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthMetric> updateMetric(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @RequestBody @Valid HealthMetricRequest request) {
        return ResponseEntity.ok(metricsService.updateMetric(id, request, userRef(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetric(Authentication authentication, @PathVariable Long id) {
        metricsService.deleteMetric(id, userRef(authentication));
        return ResponseEntity.noContent().build();
    }

    /**
     * Берём proxy User по id из X-User-Id — без похода в БД.
     * Сервис кладёт reference в FK и сравнивает по id, реальные поля User не нужны.
     */
    private User userRef(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.getReferenceById(userId);
    }
}
