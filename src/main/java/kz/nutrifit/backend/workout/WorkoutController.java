package kz.nutrifit.backend.workout;

import jakarta.validation.Valid;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService;

    public WorkoutController(WorkoutService workoutService, UserService userService) {
        this.workoutService = workoutService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@AuthenticationPrincipal UserDetails principal,
                                                 @RequestBody @Valid Workout workout) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(workoutService.createWorkout(workout, user));
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getWorkouts(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(workoutService.getWorkouts(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(@AuthenticationPrincipal UserDetails principal,
                                                 @PathVariable Long id,
                                                 @RequestBody @Valid Workout workout) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(workoutService.updateWorkout(id, workout, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = userService.getByEmail(principal.getUsername());
        workoutService.deleteWorkout(id, user);
        return ResponseEntity.noContent().build();
    }
}
