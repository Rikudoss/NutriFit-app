package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import kz.nutrifit.backend.metrics.HealthMetric;
import kz.nutrifit.backend.metrics.MetricsRepository;
import kz.nutrifit.backend.nutrition.Meal;
import kz.nutrifit.backend.nutrition.MealItem;
import kz.nutrifit.backend.nutrition.repository.MealRepository;
import kz.nutrifit.backend.profile.Profile;
import kz.nutrifit.backend.profile.ProfileRepository;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import kz.nutrifit.backend.workout.Workout;
import kz.nutrifit.backend.workout.WorkoutRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
    private final UserService userService;
    private final ProfileRepository profileRepository;
    private final MealRepository mealRepository;
    private final WorkoutRepository workoutRepository;
    private final MetricsRepository metricsRepository;

    public AIController(AIService aiService,
            UserService userService,
            ProfileRepository profileRepository,
            MealRepository mealRepository,
            WorkoutRepository workoutRepository,
            MetricsRepository metricsRepository) {
        this.aiService = aiService;
        this.userService = userService;
        this.profileRepository = profileRepository;
        this.mealRepository = mealRepository;
        this.workoutRepository = workoutRepository;
        this.metricsRepository = metricsRepository;
    }

    @PostMapping("/recommend")
    public ResponseEntity<AIResponse> recommend(@AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) AIRequest requestOverride) {
        User user = userService.getByEmail(principal.getUsername());

        String prompt;
        // Check if we should include personal data
        if (requestOverride != null && Boolean.TRUE.equals(requestOverride.getWithPersonalData())) {
            prompt = buildPrompt(user, requestOverride.getPrompt());
        } else {
            // No personal data -> just the raw user prompt with basic system context
            String userPrompt = (requestOverride != null) ? requestOverride.getPrompt() : "";
            prompt = "You are a digital fitness and nutrition coach. " +
                    "User request: " + userPrompt;
        }

        return ResponseEntity.ok(aiService.recommend(new AIRequest(prompt, false)));
    }

    private String buildPrompt(User user, String userPrompt) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "You are a digital fitness and nutrition coach. Provide concise personalized recommendations.\n");
        Profile profile = profileRepository.findByUser(user).orElse(null);
        if (profile != null) {
            builder.append("Profile: name=").append(profile.getFullName())
                    .append(", age=").append(profile.getAge())
                    .append(", gender=").append(profile.getGender())
                    .append(", height_cm=").append(profile.getHeightCm())
                    .append(", weight_kg=").append(profile.getWeightKg())
                    .append(", goal=").append(profile.getGoal())
                    .append(".\n");
        }
        List<Meal> meals = mealRepository.findAllByUser(user);
        if (!meals.isEmpty()) {
            builder.append("Recent meals: ");
            String mealSummary = meals.stream().map(this::formatMeal).collect(Collectors.joining(" | "));
            builder.append(mealSummary).append("\n");
        }
        List<Workout> workouts = workoutRepository.findAllByUser(user);
        if (!workouts.isEmpty()) {
            builder.append("Recent workouts: ");
            String workoutSummary = workouts.stream()
                    .map(w -> String.format("%s for %d minutes burning %.1f kcal", w.getType(),
                            w.getDurationMinutes(), w.getCaloriesBurned()))
                    .collect(Collectors.joining(" | "));
            builder.append(workoutSummary).append("\n");
        }
        List<HealthMetric> metrics = metricsRepository.findAllByUser(user);
        if (!metrics.isEmpty()) {
            builder.append("Wearable metrics: ");
            String metricSummary = metrics.stream()
                    .map(m -> String.format("steps=%d, heartRate=%d, caloriesBurned=%.1f, sleep=%.1f hours",
                            m.getSteps(), m.getHeartRate(), m.getCaloriesBurned(), m.getSleepHours()))
                    .collect(Collectors.joining(" | "));
            builder.append(metricSummary).append("\n");
        }
        if (userPrompt != null && !userPrompt.isBlank()) {
            builder.append("User request: ").append(userPrompt).append("\n");
        }
        builder.append("Return actionable daily nutrition and workout guidance tailored to the data.");
        return builder.toString();
    }

    private String formatMeal(Meal meal) {
        String items = meal.getItems().stream()
                .map(this::formatMealItem)
                .collect(Collectors.joining(", "));
        return String.format("%s on %s (%.1f kcal): %s", meal.getName(), meal.getMealDate(), meal.getTotalCalories(),
                items);
    }

    private String formatMealItem(MealItem item) {
        return String.format("%s %.1fg (%.1f kcal)", item.getName(),
                item.getQuantity() != null ? item.getQuantity() : 1.0,
                item.getCalories());
    }
}
