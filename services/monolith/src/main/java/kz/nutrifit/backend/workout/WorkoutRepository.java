package kz.nutrifit.backend.workout;

import kz.nutrifit.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findAllByUser(User user);
}
