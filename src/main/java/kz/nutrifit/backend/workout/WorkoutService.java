package kz.nutrifit.backend.workout;

import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public Workout createWorkout(Workout workout, User user) {
        workout.setUser(user);
        return workoutRepository.save(workout);
    }

    public List<Workout> getWorkouts(User user) {
        return workoutRepository.findAllByUser(user);
    }

    public Workout updateWorkout(Long id, Workout update, User user) {
        Workout existing = workoutRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workout not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Workout not found");
        }
        existing.setType(update.getType());
        existing.setDurationMinutes(update.getDurationMinutes());
        existing.setCaloriesBurned(update.getCaloriesBurned());
        existing.setWorkoutDate(update.getWorkoutDate());
        return workoutRepository.save(existing);
    }

    public void deleteWorkout(Long id, User user) {
        Workout existing = workoutRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workout not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Workout not found");
        }
        workoutRepository.delete(existing);
    }
}
