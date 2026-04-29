package kz.nutrifit.backend.onboarding.controller;

import kz.nutrifit.backend.onboarding.dto.OnboardingStatusResponse;
import kz.nutrifit.backend.profile.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final ProfileService profileService;

    public OnboardingController(ProfileService profileService) {
        this.profileService = profileService;
    }

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
