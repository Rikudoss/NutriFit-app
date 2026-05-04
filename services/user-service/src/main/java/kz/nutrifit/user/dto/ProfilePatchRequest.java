package kz.nutrifit.user.dto;

import kz.nutrifit.user.enums.ActivityLevel;
import kz.nutrifit.user.enums.Gender;
import kz.nutrifit.user.enums.Goal;
import lombok.Data;

@Data
public class ProfilePatchRequest {
    private String fullName;
    private Integer age;
    private Gender gender;
    private Double heightCm;
    private Double weightKg;
    private Goal goal;
    private ActivityLevel activityLevel;
}
