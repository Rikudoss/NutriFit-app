package kz.nutrifit.backend.metrics;

import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricsService {

    private final MetricsRepository metricsRepository;

    public MetricsService(MetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    public HealthMetric createMetric(HealthMetric metric, User user) {
        metric.setUser(user);
        return metricsRepository.save(metric);
    }

    public List<HealthMetric> getMetrics(User user) {
        return metricsRepository.findAllByUser(user);
    }

    public HealthMetric updateMetric(Long id, HealthMetric metricUpdate, User user) {
        HealthMetric existing = metricsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Metric not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Metric not found");
        }
        existing.setSteps(metricUpdate.getSteps());
        existing.setHeartRate(metricUpdate.getHeartRate());
        existing.setCaloriesBurned(metricUpdate.getCaloriesBurned());
        existing.setSleepHours(metricUpdate.getSleepHours());
        existing.setRecordedAt(metricUpdate.getRecordedAt());
        return metricsRepository.save(existing);
    }

    public void deleteMetric(Long id, User user) {
        HealthMetric existing = metricsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Metric not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Metric not found");
        }
        metricsRepository.delete(existing);
    }
}
