package kz.nutrifit.backend.profile;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetails principal) {
        Profile profile = profileService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(profileService.toResponse(profile));
    }

    @GetMapping("/fullname")
    public ResponseEntity<java.util.Map<String, String>> getFullName(@AuthenticationPrincipal UserDetails principal) {
        Profile profile = profileService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(java.util.Collections.singletonMap("fullName", profile.getFullName()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ProfileUpdateRequest profileRequest) {
        // Map DTO to Patch Request for compatibility, or simply use patch method logic
        // For now, allow full update via patch logic as they are similar in this
        // context
        ProfilePatchRequest patchRequest = new ProfilePatchRequest();
        patchRequest.setFullName(profileRequest.getFullName());
        patchRequest.setAge(profileRequest.getAge());
        patchRequest.setGender(profileRequest.getGender());
        patchRequest.setHeightCm(profileRequest.getHeightCm());
        patchRequest.setWeightKg(profileRequest.getWeightKg());
        patchRequest.setGoal(profileRequest.getGoal());

        return ResponseEntity.ok(profileService.patch(principal.getUsername(), patchRequest));
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> patchProfile(@AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ProfilePatchRequest req) {
        return ResponseEntity.ok(profileService.patch(principal.getUsername(), req));
    }
}
