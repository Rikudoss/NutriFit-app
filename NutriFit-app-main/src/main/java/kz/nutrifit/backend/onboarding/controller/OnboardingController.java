package kz.nutrifit.backend.onboarding.controller;

import kz.nutrifit.backend.auth.enums.Gender;
import kz.nutrifit.backend.auth.enums.Goal;
import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.onboarding.dto.OnboardingStatusResponse;
import kz.nutrifit.backend.profile.Profile;
import kz.nutrifit.backend.profile.ProfileRepository;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
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
                                p.getAge() == null     ? "AGE" :
                                        p.getGoal() == null    ? "GOAL" :
                                                "COMPLETE";

        boolean completed = "COMPLETE".equals(next);

        return ResponseEntity.ok(new OnboardingStatusResponse(completed, next, filled / 4.0));
    }

    @PostMapping("/update")
    @Transactional
    public ResponseEntity<Void> updateField(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, Object> request) {

        log.info("Updating onboarding field for user: {}", principal.getUsername());

        String field = (String) request.get("field");
        Object value = request.get("value");

        if (field == null || value == null) {
            log.warn("Invalid request: field or value is null");
            throw new IllegalArgumentException("Both 'field' and 'value' are required");
        }

        User user = userService.getByEmail(principal.getUsername());
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        try {
            switch (field.toLowerCase()) {
                case "weight":
                    Double weight = ((Number) value).doubleValue();
                    profile.setWeightKg(weight);
                    log.info("Updated weight to: {} kg", weight);
                    break;

                case "height":
                    Double height = ((Number) value).doubleValue();
                    profile.setHeightCm(height);
                    log.info("Updated height to: {} cm", height);
                    break;

                case "gender":
                    Gender gender = Gender.valueOf(value.toString().toUpperCase());
                    profile.setGender(gender);
                    log.info("Updated gender to: {}", gender);
                    break;

                case "goal":
                    Goal goal = Goal.valueOf(value.toString().toUpperCase());
                    profile.setGoal(goal);
                    log.info("Updated goal to: {}", goal);
                    break;

                case "age":
                    Integer age = ((Number) value).intValue();
                    profile.setAge(age);
                    log.info("Updated age to: {}", age);
                    break;

                default:
                    log.warn("Unknown field: {}", field);
                    throw new IllegalArgumentException("Unknown field: " + field);
            }

            profileRepository.save(profile);
            log.info("Profile updated successfully for user: {}", principal.getUsername());
        } catch (IllegalArgumentException e) {
            log.error("Error updating field '{}' with value '{}': {}", field, value, e.getMessage());
            throw new IllegalArgumentException("Invalid value for field: " + field, e);
        }

        return ResponseEntity.ok().build();
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
