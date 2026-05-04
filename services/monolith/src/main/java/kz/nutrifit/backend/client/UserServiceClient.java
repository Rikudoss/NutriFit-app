package kz.nutrifit.backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class UserServiceClient {

    private final RestTemplate internalRestTemplate;

    public UserServiceClient(@Qualifier("internalRestTemplate") RestTemplate internalRestTemplate) {
        this.internalRestTemplate = internalRestTemplate;
    }

    public ProfileSummary getProfile(Long userId) {
        try {
            return internalRestTemplate.getForObject(
                    "http://user-service/internal/users/" + userId,
                    ProfileSummary.class
            );
        } catch (Exception ex) {
            log.warn("Failed to fetch profile from user-service for userId={}: {}", userId, ex.getMessage());
            return null;
        }
    }
}
