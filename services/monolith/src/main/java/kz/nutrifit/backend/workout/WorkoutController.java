package kz.nutrifit.backend.workout;

import jakarta.validation.Valid;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserRepository userRepository;

    public WorkoutController(WorkoutService workoutService, UserRepository userRepository) {
        this.workoutService = workoutService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Workout> createWorkout(Authentication authentication,
                                                 @RequestBody @Valid WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.createWorkout(request, userRef(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getWorkouts(Authentication authentication) {
        return ResponseEntity.ok(workoutService.getWorkouts(userRef(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(Authentication authentication,
                                                 @PathVariable Long id,
                                                 @RequestBody @Valid WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.updateWorkout(id, request, userRef(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(Authentication authentication, @PathVariable Long id) {
        workoutService.deleteWorkout(id, userRef(authentication));
        return ResponseEntity.noContent().build();
    }

    private User userRef(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.getReferenceById(userId);
    }
}
