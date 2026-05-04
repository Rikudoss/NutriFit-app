package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import kz.nutrifit.backend.client.ProfileSummary;
import kz.nutrifit.backend.client.UserServiceClient;
import kz.nutrifit.backend.metrics.HealthMetric;
import kz.nutrifit.backend.metrics.MetricsRepository;
import kz.nutrifit.backend.nutrition.Meal;
import kz.nutrifit.backend.nutrition.MealItem;
import kz.nutrifit.backend.nutrition.repository.MealRepository;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.workout.Workout;
import kz.nutrifit.backend.workout.WorkoutRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final UserServiceClient userServiceClient;
    private final MealRepository mealRepository;
    private final WorkoutRepository workoutRepository;
    private final MetricsRepository metricsRepository;

    public AIService(RestTemplate openAiRestTemplate,
                     UserServiceClient userServiceClient,
                     MealRepository mealRepository,
                     WorkoutRepository workoutRepository,
                     MetricsRepository metricsRepository) {
        this.restTemplate = openAiRestTemplate;
        this.userServiceClient = userServiceClient;
        this.mealRepository = mealRepository;
        this.workoutRepository = workoutRepository;
        this.metricsRepository = metricsRepository;
    }

    public String buildContext(User user, String userPrompt) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a digital fitness and nutrition coach. Provide concise personalized recommendations.\n");

        ProfileSummary profile = userServiceClient.getProfile(user.getId());
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
            String mealSummary = meals.stream().map(this::formatMeal).collect(Collectors.joining(" | "));
            builder.append("Recent meals: ").append(mealSummary).append("\n");
        }

        List<Workout> workouts = workoutRepository.findAllByUser(user);
        if (!workouts.isEmpty()) {
            String workoutSummary = workouts.stream()
                    .map(w -> String.format("%s for %d minutes burning %.1f kcal",
                            w.getType(), w.getDurationMinutes(), w.getCaloriesBurned()))
                    .collect(Collectors.joining(" | "));
            builder.append("Recent workouts: ").append(workoutSummary).append("\n");
        }

        List<HealthMetric> metrics = metricsRepository.findAllByUser(user);
        if (!metrics.isEmpty()) {
            String metricSummary = metrics.stream()
                    .map(m -> String.format("steps=%d, heartRate=%d, caloriesBurned=%.1f, sleep=%.1f hours",
                            m.getSteps(), m.getHeartRate(), m.getCaloriesBurned(), m.getSleepHours()))
                    .collect(Collectors.joining(" | "));
            builder.append("Wearable metrics: ").append(metricSummary).append("\n");
        }

        if (userPrompt != null && !userPrompt.isBlank()) {
            builder.append("User request: ").append(userPrompt).append("\n");
        }
        builder.append("Return actionable daily nutrition and workout guidance tailored to the data.");
        return builder.toString();
    }

    public AIResponse recommend(AIRequest request) {
        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", request.getPrompt())),
                "temperature", 0.7
        );
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions", body, Map.class);
        String recommendation = extractRecommendation(response.getBody());
        return new AIResponse(recommendation);
    }

    @SuppressWarnings("unchecked")
    private String extractRecommendation(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return "No response received from AI service.";
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message != null && message.get("content") != null) {
                return message.get("content").toString();
            }
        }
        return "AI recommendation unavailable at the moment.";
    }

    private String formatMeal(Meal meal) {
        String items = meal.getItems().stream()
                .map(this::formatMealItem)
                .collect(Collectors.joining(", "));
        return String.format("%s on %s (%.1f kcal): %s",
                meal.getName(), meal.getMealDate(), meal.getTotalCalories(), items);
    }

    private String formatMealItem(MealItem item) {
        double qty = item.getQuantity() != null ? item.getQuantity() : 0.0;
        double kcal = item.getCaloriesPer100g() != null ? item.getCaloriesPer100g() * qty / 100.0 : 0.0;
        return String.format("%s %.1fg (%.1f kcal)", item.getName(), qty, kcal);
    }
}
