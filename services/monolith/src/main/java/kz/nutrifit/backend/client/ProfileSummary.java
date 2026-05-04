package kz.nutrifit.backend.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileSummary {
    private String fullName;
    private Integer age;
    private String gender;
    private Double heightCm;
    private Double weightKg;
    private String goal;
    private String activityLevel;
}
