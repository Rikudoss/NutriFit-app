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

    public Workout createWorkout(WorkoutRequest request, User user) {
        Workout workout = new Workout();
        workout.setType(request.getType());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setCaloriesBurned(request.getCaloriesBurned());
        workout.setWorkoutDate(request.getWorkoutDate());
        workout.setUser(user);
        return workoutRepository.save(workout);
    }

    public List<Workout> getWorkouts(User user) {
        return workoutRepository.findAllByUser(user);
    }

    public Workout updateWorkout(Long id, WorkoutRequest request, User user) {
        Workout existing = workoutRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workout not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Workout not found");
        }
        existing.setType(request.getType());
        existing.setDurationMinutes(request.getDurationMinutes());
        existing.setCaloriesBurned(request.getCaloriesBurned());
        existing.setWorkoutDate(request.getWorkoutDate());
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
