package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import kz.nutrifit.backend.client.MealItemSummary;
import kz.nutrifit.backend.client.MealSummary;
import kz.nutrifit.backend.client.NutritionServiceClient;
import kz.nutrifit.backend.client.ProfileSummary;
import kz.nutrifit.backend.client.UserServiceClient;
import kz.nutrifit.backend.metrics.HealthMetric;
import kz.nutrifit.backend.metrics.MetricsRepository;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.workout.Workout;
import kz.nutrifit.backend.workout.WorkoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String SYSTEM_PROMPT =
            "You are NutriFit AI — a smart fitness and nutrition assistant. " +
            "Answer the user's question directly. If the question is about fitness, nutrition, or health — " +
            "use the provided profile and meal data for personalized advice. " +
            "If the question is general (math, weather, etc.) — answer it normally without fitness context. " +
            "IMPORTANT: Always respond in the same language the user writes in. " +
            "If user writes in Russian, respond in Russian. If in English, respond in English.";

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:500}")
    private int maxTokens;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final UserServiceClient userServiceClient;
    private final NutritionServiceClient nutritionServiceClient;
    private final WorkoutRepository workoutRepository;
    private final MetricsRepository metricsRepository;

    public AIService(RestTemplate openAiRestTemplate,
                     UserServiceClient userServiceClient,
                     NutritionServiceClient nutritionServiceClient,
                     WorkoutRepository workoutRepository,
                     MetricsRepository metricsRepository) {
        this.restTemplate = openAiRestTemplate;
        this.userServiceClient = userServiceClient;
        this.nutritionServiceClient = nutritionServiceClient;
        this.workoutRepository = workoutRepository;
        this.metricsRepository = metricsRepository;
    }

    public String buildContext(User user, String userPrompt) {
        StringBuilder builder = new StringBuilder();

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

        List<MealSummary> meals = nutritionServiceClient.getMeals(user.getId());
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
        builder.append("Answer the user's question using this context if relevant.");
        return builder.toString();
    }

    public AIResponse recommend(AIRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", request.getPrompt())
                    ),
                    "temperature", temperature,
                    "max_tokens", maxTokens
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);
            return new AIResponse(extractRecommendation(response.getBody()));
        } catch (Exception ex) {
            log.error("OpenAI call failed", ex);
            return new AIResponse("AI recommendation unavailable at the moment.");
        }
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

    private String formatMeal(MealSummary meal) {
        String items = meal.getItems() != null
                ? meal.getItems().stream().map(this::formatMealItem).collect(Collectors.joining(", "))
                : "";
        return String.format("%s on %s (%.1f kcal): %s",
                meal.getName(), meal.getMealDate(), meal.getTotalCalories(), items);
    }

    private String formatMealItem(MealItemSummary item) {
        double qty = item.getQuantity() != null ? item.getQuantity() : 0.0;
        double kcal = item.getCaloriesPer100g() != null ? item.getCaloriesPer100g() * qty / 100.0 : 0.0;
        return String.format("%s %.1fg (%.1f kcal)", item.getName(), qty, kcal);
    }
}
