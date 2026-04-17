package kz.nutrifit.backend.onboarding.controller;

import kz.nutrifit.backend.onboarding.dto.OnboardingStatusResponse;
import kz.nutrifit.backend.profile.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final ProfileService profileService;

    public OnboardingController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<OnboardingStatusResponse> status(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(profileService.getOnboardingStatus(principal.getUsername()));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> complete(@AuthenticationPrincipal UserDetails principal) {
        profileService.completeOnboarding(principal.getUsername());
        return ResponseEntity.ok().build();
    }
}
