package kz.nutrifit.backend.onboarding.controller;

import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.onboarding.dto.OnboardingStatusResponse;
import kz.nutrifit.backend.profile.Profile;
import kz.nutrifit.backend.profile.ProfileRepository;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final ProfileRepository profileRepository;
    private final UserService userService;

    public OnboardingController(ProfileRepository profileRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<OnboardingStatusResponse> status(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        Profile p = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        int filled = 0;
        if (p.getGender() != null) filled++;
        if (p.getWeightKg() != null) filled++;
        if (p.getAge() != null) filled++;
        if (p.getGoal() != null) filled++;

        String next =
                p.getGender() == null ? "GENDER" :
                        p.getWeightKg() == null ? "WEIGHT" :
                                p.getAge() == null ? "AGE" :
                                        p.getGoal() == null ? "GOAL" :
                                                "COMPLETE";

        boolean completed = "COMPLETE".equals(next);

        return ResponseEntity.ok(new OnboardingStatusResponse(completed, next, filled / 4.0));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> complete(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        Profile p = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        if (p.getGender() == null || p.getWeightKg() == null || p.getAge() == null || p.getGoal() == null) {
            throw new IllegalStateException("Onboarding not finished");
        }

        p.setOnboardingCompleted(true);
        profileRepository.save(p);

        return ResponseEntity.ok().build();
    }
}
