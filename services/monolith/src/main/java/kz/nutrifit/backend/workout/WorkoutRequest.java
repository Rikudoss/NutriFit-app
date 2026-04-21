package kz.nutrifit.backend.workout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkoutRequest {

    @NotBlank
    private String type;

    @NotNull
    @Positive
    private Integer durationMinutes;

    @NotNull
    @PositiveOrZero
    private Double caloriesBurned;

    @NotNull
    private LocalDateTime workoutDate;
}
