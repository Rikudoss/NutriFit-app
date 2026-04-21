package kz.nutrifit.backend.profile;// kz.nutrifit.backend.profile.dto.ProfileResponse
import kz.nutrifit.backend.auth.enums.Gender;
import kz.nutrifit.backend.auth.enums.Goal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String fullName;
    private Integer age;
    private Gender gender;
    private Double heightCm;
    private Double weightKg;
    private Goal goal;
    private boolean onboardingCompleted;
}
