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
        return ResponseEntity.ok(profileService.toResponse(profileService.getByEmail(principal.getUsername())));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails principal,
                                                         @RequestBody @Valid ProfilePatchRequest req) {
        return ResponseEntity.ok(profileService.update(principal.getUsername(), req));
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> patchProfile(@AuthenticationPrincipal UserDetails principal,
                                                        @RequestBody @Valid ProfilePatchRequest req) {
        return ResponseEntity.ok(profileService.patch(principal.getUsername(), req));
    }
}
