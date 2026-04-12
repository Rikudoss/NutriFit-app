package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate;

    public AIService(RestTemplate openAiRestTemplate) {
        this.restTemplate = openAiRestTemplate;
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
}
