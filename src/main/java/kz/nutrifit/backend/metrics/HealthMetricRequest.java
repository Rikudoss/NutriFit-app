package kz.nutrifit.backend.metrics;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthMetricRequest {

    @PositiveOrZero
    private Integer steps;

    @PositiveOrZero
    private Integer heartRate;

    @PositiveOrZero
    private Double caloriesBurned;

    @PositiveOrZero
    private Double sleepHours;

    @NotNull
    private LocalDateTime recordedAt;
}
