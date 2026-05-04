package kz.nutrifit.user.controller;

import kz.nutrifit.user.dto.OnboardingStatusResponse;
import kz.nutrifit.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<OnboardingStatusResponse> status(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getOnboardingStatus(userId));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> complete(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        profileService.completeOnboarding(userId);
        return ResponseEntity.ok().build();
    }
}
