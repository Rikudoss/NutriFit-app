package kz.nutrifit.user.service;

import kz.nutrifit.user.dto.OnboardingStatusResponse;
import kz.nutrifit.user.dto.ProfilePatchRequest;
import kz.nutrifit.user.dto.ProfileResponse;
import kz.nutrifit.user.entity.Profile;
import kz.nutrifit.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public Profile getOrCreate(Long userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile profile = Profile.builder().userId(userId).build();
            return profileRepository.save(profile);
        });
    }

    public ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getUserId(),
                p.getFullName(),
                p.getAge(),
                p.getGender(),
                p.getHeightCm(),
                p.getWeightKg(),
                p.getGoal(),
                p.getActivityLevel(),
                p.isOnboardingCompleted()
        );
    }

    @Transactional
    public ProfileResponse patch(Long userId, ProfilePatchRequest req) {
        Profile p = getOrCreate(userId);
        if (req.getFullName() != null) p.setFullName(req.getFullName());
        if (req.getGender() != null) p.setGender(req.getGender());
        if (req.getAge() != null) p.setAge(req.getAge());
        if (req.getHeightCm() != null) p.setHeightCm(req.getHeightCm());
        if (req.getWeightKg() != null) p.setWeightKg(req.getWeightKg());
        if (req.getGoal() != null) p.setGoal(req.getGoal());
        if (req.getActivityLevel() != null) p.setActivityLevel(req.getActivityLevel());
        return toResponse(p);
    }

    @Transactional
    public ProfileResponse update(Long userId, ProfilePatchRequest req) {
        Profile p = getOrCreate(userId);
        p.setFullName(req.getFullName());
        p.setAge(req.getAge());
        p.setGender(req.getGender());
        p.setHeightCm(req.getHeightCm());
        p.setWeightKg(req.getWeightKg());
        p.setGoal(req.getGoal());
        p.setActivityLevel(req.getActivityLevel());
        return toResponse(p);
    }

    @Transactional
    public OnboardingStatusResponse getOnboardingStatus(Long userId) {
        Profile p = getOrCreate(userId);
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
    public void completeOnboarding(Long userId) {
        Profile p = getOrCreate(userId);
        if (p.getGender() == null || p.getWeightKg() == null || p.getAge() == null || p.getGoal() == null) {
            throw new IllegalStateException("Onboarding not finished");
        }
        if (p.getAge() < 5 || p.getAge() > 120) throw new IllegalStateException("Invalid age");
        if (p.getWeightKg() < 20 || p.getWeightKg() > 350) throw new IllegalStateException("Invalid weight");
        p.setOnboardingCompleted(true);
    }
}
