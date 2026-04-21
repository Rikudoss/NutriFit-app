package kz.nutrifit.backend.nutrition;

import jakarta.validation.Valid;
import kz.nutrifit.backend.nutrition.dto.CreateMealRequest;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;
    private final UserService userService;

    public NutritionController(NutritionService nutritionService, UserService userService) {
        this.nutritionService = nutritionService;
        this.userService = userService;
    }

    @PostMapping("/meals")
    public ResponseEntity<Meal> createMeal(@AuthenticationPrincipal UserDetails principal,
                                           @RequestBody @Valid CreateMealRequest request) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(nutritionService.createMeal(request, user));
    }

    @GetMapping("/meals")
    public ResponseEntity<List<Meal>> getMeals(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(nutritionService.getMeals(user));
    }

    @GetMapping("/meals/{id}")
    public ResponseEntity<Meal> getMeal(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(nutritionService.getMeal(id, user));
    }

    @PutMapping("/meals/{id}")
    public ResponseEntity<Meal> updateMeal(@AuthenticationPrincipal UserDetails principal,
                                           @PathVariable Long id,
                                           @RequestBody @Valid CreateMealRequest request) {
        User user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(nutritionService.updateMeal(id, request, user));
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> deleteMeal(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = userService.getByEmail(principal.getUsername());
        nutritionService.deleteMeal(id, user);
        return ResponseEntity.noContent().build();
    }
}
