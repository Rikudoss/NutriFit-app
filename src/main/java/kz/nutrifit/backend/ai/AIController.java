package kz.nutrifit.backend.ai;

import kz.nutrifit.backend.ai.dto.AIRequest;
import kz.nutrifit.backend.ai.dto.AIResponse;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
    private final UserService userService;

    public AIController(AIService aiService, UserService userService) {
        this.aiService = aiService;
        this.userService = userService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<AIResponse> recommend(@AuthenticationPrincipal UserDetails principal,
                                                @RequestBody(required = false) AIRequest requestOverride) {
        User user = userService.getByEmail(principal.getUsername());
        String prompt = aiService.buildContext(user, requestOverride != null ? requestOverride.getPrompt() : null);
        return ResponseEntity.ok(aiService.recommend(new AIRequest(prompt)));
    }
}
