package kz.nutrifit.backend.nutrition.repository;

import kz.nutrifit.backend.nutrition.Meal;
import kz.nutrifit.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findAllByUser(User user);
}
