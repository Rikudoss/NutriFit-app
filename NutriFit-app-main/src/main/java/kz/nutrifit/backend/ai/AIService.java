package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import kz.nutrifit.backend.ai.dto.OpenAIChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate;

    public AIService(RestTemplate openAiRestTemplate) {
        this.restTemplate = openAiRestTemplate;
    }

    public AIResponse recommend(AIRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(
                Map.of("role", "user", "content", request.getPrompt())
        ));
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // только это

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OpenAIChatResponse> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    OpenAIChatResponse.class
            );

            String recommendation = extractRecommendation(response.getBody());
            return new AIResponse(recommendation);
        } catch (Exception e) {
            e.printStackTrace();
            return new AIResponse("Error connecting to AI service: " + e.getMessage());
        }
    }

    private String extractRecommendation(OpenAIChatResponse responseBody) {
        if (responseBody == null || responseBody.getChoices() == null || responseBody.getChoices().isEmpty()) {
            return "No advice available at this time.";
        }
        return responseBody.getChoices().get(0).getMessage().getContent();
    }
}
