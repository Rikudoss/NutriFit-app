package kz.nutrifit.nutrition.controller;

import jakarta.validation.Valid;
import kz.nutrifit.nutrition.dto.CreateMealRequest;
import kz.nutrifit.nutrition.dto.DailySummaryResponse;
import kz.nutrifit.nutrition.dto.NutritionGoalRequest;
import kz.nutrifit.nutrition.entity.Meal;
import kz.nutrifit.nutrition.entity.NutritionGoal;
import kz.nutrifit.nutrition.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    @PostMapping("/meals")
    public ResponseEntity<Meal> createMeal(Authentication authentication,
                                           @RequestBody @Valid CreateMealRequest request) {
        return ResponseEntity.ok(nutritionService.createMeal(request, userId(authentication)));
    }

    @GetMapping("/meals")
    public ResponseEntity<List<Meal>> getMeals(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.getMeals(userId(authentication)));
    }

    @GetMapping("/meals/{id}")
    public ResponseEntity<Meal> getMeal(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(nutritionService.getMeal(id, userId(authentication)));
    }

    @PutMapping("/meals/{id}")
    public ResponseEntity<Meal> updateMeal(Authentication authentication,
                                           @PathVariable Long id,
                                           @RequestBody @Valid CreateMealRequest request) {
        return ResponseEntity.ok(nutritionService.updateMeal(id, request, userId(authentication)));
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> deleteMeal(Authentication authentication, @PathVariable Long id) {
        nutritionService.deleteMeal(id, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/goals")
    public ResponseEntity<NutritionGoal> getGoals(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.getGoals(userId(authentication)));
    }

    @PutMapping("/goals")
    public ResponseEntity<NutritionGoal> updateGoals(Authentication authentication,
                                                      @RequestBody NutritionGoalRequest request) {
        return ResponseEntity.ok(nutritionService.updateGoals(request, userId(authentication)));
    }

    @GetMapping("/summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutritionService.getDailySummary(userId(authentication), date));
    }

    private Long userId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
