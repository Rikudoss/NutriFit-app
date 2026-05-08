package kz.nutrifit.nutrition.repository;

import kz.nutrifit.nutrition.entity.NutritionGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, Long> {

    Optional<NutritionGoal> findByUserId(Long userId);
}
