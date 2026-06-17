package kz.nutrifit.backend.metrics;

import jakarta.validation.Valid;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;
    private final UserService userService;

    public MetricsController(MetricsService metricsService, UserService userService) {
        this.metricsService = metricsService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<HealthMetric> createMetric(@AuthenticationPrincipal UserDetails principal,
                                                     @RequestBody @Valid HealthMetric metric) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(metricsService.createMetric(metric, user));
    }

    @GetMapping
    public ResponseEntity<List<HealthMetric>> getMetrics(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(metricsService.getMetrics(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthMetric> updateMetric(@AuthenticationPrincipal UserDetails principal,
                                                     @PathVariable Long id,
                                                     @RequestBody @Valid HealthMetric metric) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(metricsService.updateMetric(id, metric, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetric(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = userService.getByEmail(principal.getUsername());
        metricsService.deleteMetric(id, user);
        return ResponseEntity.noContent().build();
    }
}
