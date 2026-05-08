package kz.nutrifit.nutrition.repository;

import kz.nutrifit.nutrition.entity.Meal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<Meal, Long> {

    @EntityGraph(attributePaths = "items")
    List<Meal> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = "items")
    Optional<Meal> findByIdAndUserId(Long id, Long userId);

    List<Meal> findAllByUserIdAndMealDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = "items")
    List<Meal> findTop50ByUserIdOrderByMealDateDesc(Long userId);
}
