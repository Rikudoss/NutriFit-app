package kz.nutrifit.backend.metrics;

import kz.nutrifit.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetricsRepository extends JpaRepository<HealthMetric, Long> {
    List<HealthMetric> findAllByUser(User user);
}
