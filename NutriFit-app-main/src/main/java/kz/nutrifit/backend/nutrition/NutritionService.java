package kz.nutrifit.backend.nutrition;

import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.nutrition.dto.CreateMealRequest;
import kz.nutrifit.backend.nutrition.dto.MealItemRequest;
import kz.nutrifit.backend.nutrition.repository.MealItemRepository;
import kz.nutrifit.backend.nutrition.repository.MealRepository;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.util.CaloriesCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NutritionService {

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;

    public NutritionService(MealRepository mealRepository, MealItemRepository mealItemRepository) {
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
    }

    public List<Meal> getMeals(User user) {
        return mealRepository.findAllByUser(user);
    }

    public Meal getMeal(Long id, User user) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Meal not found"));
        if (!meal.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Meal not found");
        }
        return meal;
    }

    @Transactional
    public Meal createMeal(CreateMealRequest request, User user) {
        Meal meal = Meal.builder()
                .name(request.getName())
                .mealDate(request.getMealDate())
                .user(user)
                .build();
        mealRepository.save(meal);
        addItemsToMeal(meal, request.getItems());
        meal.setTotalCalories(CaloriesCalculator.calculateTotalCalories(meal.getItems()));
        return mealRepository.save(meal);
    }

    @Transactional
    public Meal updateMeal(Long id, CreateMealRequest request, User user) {
        Meal meal = getMeal(id, user);
        meal.getItems().clear();
        meal.setName(request.getName());
        meal.setMealDate(request.getMealDate());
        addItemsToMeal(meal, request.getItems());
        meal.setTotalCalories(CaloriesCalculator.calculateTotalCalories(meal.getItems()));
        return mealRepository.save(meal);
    }

    @Transactional
    public void deleteMeal(Long id, User user) {
        Meal meal = getMeal(id, user);
        mealRepository.delete(meal);
    }

    private void addItemsToMeal(Meal meal, List<MealItemRequest> items) {
        for (MealItemRequest itemRequest : items) {
            MealItem item = MealItem.builder()
                    .name(itemRequest.getName())
                    .calories(itemRequest.getCalories())
                    .protein(itemRequest.getProtein())
                    .carbs(itemRequest.getCarbs())
                    .fat(itemRequest.getFat())
                    .quantity(itemRequest.getQuantity())
                    .meal(meal)
                    .build();
            meal.getItems().add(item);
            mealItemRepository.save(item);
        }
    }
}
