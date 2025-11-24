package kz.nutrifit.backend.nutrition.repository;

import kz.nutrifit.backend.nutrition.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {
}
