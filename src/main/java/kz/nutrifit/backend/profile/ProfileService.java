package kz.nutrifit.backend.profile;

import kz.nutrifit.backend.onboarding.dto.OnboardingStatusResponse;
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
    public ProfileResponse update(String email, ProfilePatchRequest req) {
        Profile p = getByEmail(email);
        p.setFullName(req.getFullName());
        p.setAge(req.getAge());
        p.setGender(req.getGender());
        p.setHeightCm(req.getHeightCm());
        p.setWeightKg(req.getWeightKg());
        p.setGoal(req.getGoal());
        return toResponse(p);
    }

    public OnboardingStatusResponse getOnboardingStatus(String email) {
        Profile p = getByEmail(email);

        int filled = 0;
        if (p.getGender() != null) filled++;
        if (p.getWeightKg() != null) filled++;
        if (p.getAge() != null) filled++;
        if (p.getGoal() != null) filled++;

        String next =
                p.getGender() == null ? "GENDER" :
                p.getWeightKg() == null ? "WEIGHT" :
                p.getAge() == null ? "AGE" :
                p.getGoal() == null ? "GOAL" : "COMPLETE";

        return new OnboardingStatusResponse("COMPLETE".equals(next), next, filled / 4.0);
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
