package kz.nutrifit.nutrition.repository;

import kz.nutrifit.nutrition.entity.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {
}
