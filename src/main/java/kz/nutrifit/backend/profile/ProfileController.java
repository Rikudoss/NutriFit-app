package kz.nutrifit.backend.profile;

import jakarta.validation.Valid;
import kz.nutrifit.backend.exception.NotFoundException;
import kz.nutrifit.backend.user.User;
import kz.nutrifit.backend.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
