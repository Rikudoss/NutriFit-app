package kz.nutrifit.backend.nutrition;

import jakarta.validation.Valid;
import kz.nutrifit.backend.nutrition.dto.CreateMealRequest;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;
    private final UserRepository userRepository;

    public NutritionController(NutritionService nutritionService, UserRepository userRepository) {
        this.nutritionService = nutritionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/meals")
    public ResponseEntity<Meal> createMeal(Authentication authentication,
                                           @RequestBody @Valid CreateMealRequest request) {
        return ResponseEntity.ok(nutritionService.createMeal(request, userRef(authentication)));
    }

    @GetMapping("/meals")
    public ResponseEntity<List<Meal>> getMeals(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.getMeals(userRef(authentication)));
    }

    @GetMapping("/meals/{id}")
    public ResponseEntity<Meal> getMeal(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(nutritionService.getMeal(id, userRef(authentication)));
    }

    @PutMapping("/meals/{id}")
    public ResponseEntity<Meal> updateMeal(Authentication authentication,
                                           @PathVariable Long id,
                                           @RequestBody @Valid CreateMealRequest request) {
        return ResponseEntity.ok(nutritionService.updateMeal(id, request, userRef(authentication)));
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> deleteMeal(Authentication authentication, @PathVariable Long id) {
        nutritionService.deleteMeal(id, userRef(authentication));
        return ResponseEntity.noContent().build();
    }

    private User userRef(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.getReferenceById(userId);
    }
}
