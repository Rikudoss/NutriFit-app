package kz.nutrifit.nutrition.service;

import kz.nutrifit.nutrition.dto.CreateMealRequest;
import kz.nutrifit.nutrition.dto.DailySummaryResponse;
import kz.nutrifit.nutrition.dto.MealItemRequest;
import kz.nutrifit.nutrition.dto.NutritionGoalRequest;
import kz.nutrifit.nutrition.entity.Meal;
import kz.nutrifit.nutrition.entity.MealItem;
import kz.nutrifit.nutrition.entity.NutritionGoal;
import kz.nutrifit.nutrition.exception.NotFoundException;
import kz.nutrifit.nutrition.repository.MealItemRepository;
import kz.nutrifit.nutrition.repository.MealRepository;
import kz.nutrifit.nutrition.repository.NutritionGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final NutritionGoalRepository goalRepository;

    @Transactional(readOnly = true)
    public List<Meal> getMeals(Long userId) {
        return mealRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Meal getMeal(Long id, Long userId) {
        return mealRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Meal not found"));
    }

    @Transactional
    public Meal createMeal(CreateMealRequest request, Long userId) {
        Meal meal = Meal.builder()
                .userId(userId)
                .name(request.getName())
                .mealType(request.getMealType())
                .mealDate(request.getMealDate())
                .build();
        mealRepository.save(meal);
        addItemsToMeal(meal, request.getItems());
        calculateTotals(meal);
        return mealRepository.save(meal);
    }

    @Transactional
    public Meal updateMeal(Long id, CreateMealRequest request, Long userId) {
        Meal meal = getMeal(id, userId);
        meal.getItems().clear();
        meal.setName(request.getName());
        meal.setMealType(request.getMealType());
        meal.setMealDate(request.getMealDate());
        addItemsToMeal(meal, request.getItems());
        calculateTotals(meal);
        return mealRepository.save(meal);
    }

    @Transactional
    public void deleteMeal(Long id, Long userId) {
        Meal meal = getMeal(id, userId);
        mealRepository.delete(meal);
    }

    @Transactional
    public NutritionGoal getGoals(Long userId) {
        return goalRepository.findByUserId(userId)
                .orElseGet(() -> goalRepository.save(
                        NutritionGoal.builder().userId(userId).build()));
    }

    @Transactional
    public NutritionGoal updateGoals(NutritionGoalRequest request, Long userId) {
        NutritionGoal goal = goalRepository.findByUserId(userId)
                .orElse(NutritionGoal.builder().userId(userId).build());
        goal.setDailyCalories(request.getDailyCalories());
        goal.setProteinTarget(request.getProteinTarget());
        goal.setCarbsTarget(request.getCarbsTarget());
        goal.setFatTarget(request.getFatTarget());
        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public DailySummaryResponse getDailySummary(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Meal> meals = mealRepository.findAllByUserIdAndMealDateBetween(userId, start, end);

        double totalCalories = meals.stream()
                .mapToDouble(m -> m.getTotalCalories() != null ? m.getTotalCalories() : 0.0).sum();
        double totalProtein = meals.stream()
                .mapToDouble(m -> m.getTotalProtein() != null ? m.getTotalProtein() : 0.0).sum();
        double totalCarbs = meals.stream()
                .mapToDouble(m -> m.getTotalCarbs() != null ? m.getTotalCarbs() : 0.0).sum();
        double totalFat = meals.stream()
                .mapToDouble(m -> m.getTotalFat() != null ? m.getTotalFat() : 0.0).sum();

        NutritionGoal goal = goalRepository.findByUserId(userId).orElse(null);

        return DailySummaryResponse.builder()
                .date(date)
                .consumedCalories(totalCalories)
                .consumedProtein(totalProtein)
                .consumedCarbs(totalCarbs)
                .consumedFat(totalFat)
                .mealCount(meals.size())
                .goalCalories(goal != null ? goal.getDailyCalories() : null)
                .goalProtein(goal != null ? goal.getProteinTarget() : null)
                .goalCarbs(goal != null ? goal.getCarbsTarget() : null)
                .goalFat(goal != null ? goal.getFatTarget() : null)
                .remainingCalories(goal != null && goal.getDailyCalories() != null
                        ? goal.getDailyCalories() - totalCalories : null)
                .build();
    }

    private void addItemsToMeal(Meal meal, List<MealItemRequest> items) {
        List<MealItem> mealItems = items.stream()
                .map(req -> MealItem.builder()
                        .meal(meal)
                        .name(req.getName())
                        .caloriesPer100g(req.getCaloriesPer100g())
                        .protein(req.getProtein())
                        .carbs(req.getCarbs())
                        .fat(req.getFat())
                        .quantity(req.getQuantity())
                        .build())
                .toList();
        meal.getItems().addAll(mealItems);
        mealItemRepository.saveAll(mealItems);
    }

    private void calculateTotals(Meal meal) {
        List<MealItem> items = meal.getItems();
        meal.setTotalCalories(sumPer100g(items, MealItem::getCaloriesPer100g));
        meal.setTotalProtein(sumPer100g(items, MealItem::getProtein));
        meal.setTotalCarbs(sumPer100g(items, MealItem::getCarbs));
        meal.setTotalFat(sumPer100g(items, MealItem::getFat));
    }

    private double sumPer100g(List<MealItem> items, java.util.function.Function<MealItem, Double> valueFn) {
        return items.stream()
                .mapToDouble(item -> {
                    Double v = valueFn.apply(item);
                    return (v != null && item.getQuantity() != null) ? v * item.getQuantity() / 100.0 : 0.0;
                })
                .sum();
    }
}
