package kz.nutrifit.backend.profile;// kz.nutrifit.backend.profile.ProfileService
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Profile getByEmail(String email) {
        return profileRepository.findByUser_Email(email)
                .orElseThrow(() -> new IllegalStateException("Profile not found for user: " + email));
    }

    public ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getFullName(),
                p.getAge(),
                p.getGender(),
                p.getHeightCm(),
                p.getWeightKg(),
                p.getGoal(),
                p.isOnboardingCompleted()
        );
    }

    @Transactional
    public ProfileResponse patch(String email, ProfilePatchRequest req) {
        Profile p = getByEmail(email);

        if (req.getFullName() != null) p.setFullName(req.getFullName());
        if (req.getGender() != null) p.setGender(req.getGender());
        if (req.getAge() != null) p.setAge(req.getAge());
        if (req.getHeightCm() != null) p.setHeightCm(req.getHeightCm());
        if (req.getWeightKg() != null) p.setWeightKg(req.getWeightKg());
        if (req.getGoal() != null) p.setGoal(req.getGoal());

        // JPA сохранит сам в конце транзакции
        return toResponse(p);
    }

    @Transactional
    public void completeOnboarding(String email) {
        Profile p = getByEmail(email);

        // обязательные шаги для твоего онбординга:
        if (p.getGender() == null || p.getWeightKg() == null || p.getAge() == null || p.getGoal() == null) {
            throw new IllegalStateException("Onboarding not finished");
        }

        // базовая валидация (чтобы не мусор)
        if (p.getAge() < 5 || p.getAge() > 120) throw new IllegalStateException("Invalid age");
        if (p.getWeightKg() < 20 || p.getWeightKg() > 350) throw new IllegalStateException("Invalid weight");

        p.setOnboardingCompleted(true);
    }
}
