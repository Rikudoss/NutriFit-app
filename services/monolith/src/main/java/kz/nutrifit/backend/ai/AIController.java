package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
    private final UserRepository userRepository;

    public AIController(AIService aiService, UserRepository userRepository) {
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/recommend")
    public ResponseEntity<AIResponse> recommend(Authentication authentication,
                                                @RequestBody(required = false) AIRequest requestOverride) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.getReferenceById(userId);
        String prompt = aiService.buildContext(user, requestOverride != null ? requestOverride.getPrompt() : null);
        return ResponseEntity.ok(aiService.recommend(new AIRequest(prompt)));
    }
}
