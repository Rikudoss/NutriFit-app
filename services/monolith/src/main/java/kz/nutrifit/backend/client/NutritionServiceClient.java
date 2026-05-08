package kz.nutrifit.backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class NutritionServiceClient {

    private final RestTemplate internalRestTemplate;

    public NutritionServiceClient(@Qualifier("internalRestTemplate") RestTemplate internalRestTemplate) {
        this.internalRestTemplate = internalRestTemplate;
    }

    public List<MealSummary> getMeals(Long userId) {
        try {
            return internalRestTemplate.exchange(
                    "http://nutrition-service/internal/nutrition/users/" + userId + "/meals",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MealSummary>>() {}
            ).getBody();
        } catch (Exception ex) {
            log.warn("Failed to fetch meals from nutrition-service for userId={}: {}", userId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
