package kz.nutrifit.backend.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardingStatusResponse {
    private boolean completed;
    private String nextStep;   // GENDER | WEIGHT | AGE | GOAL | COMPLETE
    private double progress;   // 0..1
}
