package kz.nutrifit.backend.profile;

import jakarta.validation.Valid;
import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final UserService userService;

    public ProfileController(ProfileRepository profileRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Profile> getProfile(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByEmail(principal.getUsername());
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<Profile> updateProfile(@AuthenticationPrincipal UserDetails principal,
                                                 @RequestBody @Valid Profile profileRequest) {
        User user = userService.getByEmail(principal.getUsername());
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        profile.setFullName(profileRequest.getFullName());
        profile.setAge(profileRequest.getAge());
        profile.setGender(profileRequest.getGender());
        profile.setHeightCm(profileRequest.getHeightCm());
        profile.setWeightKg(profileRequest.getWeightKg());
        profile.setGoal(profileRequest.getGoal());
        return ResponseEntity.ok(profileRepository.save(profile));
    }
    @PatchMapping
    public ResponseEntity<Profile> patchProfile(@AuthenticationPrincipal UserDetails principal,
                                                @RequestBody @Valid ProfilePatchRequest req) {
        User user = userService.getByEmail(principal.getUsername());
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        if (req.getFullName() != null) profile.setFullName(req.getFullName());
        if (req.getAge() != null) profile.setAge(req.getAge());
        if (req.getGender() != null) profile.setGender(req.getGender());
        if (req.getHeightCm() != null) profile.setHeightCm(req.getHeightCm());
        if (req.getWeightKg() != null) profile.setWeightKg(req.getWeightKg());
        if (req.getGoal() != null) profile.setGoal(req.getGoal());

        return ResponseEntity.ok(profileRepository.save(profile));
    }
}
