package kz.nutrifit.backend.profile;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.toResponse(profileService.getOrCreate(userId)));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication,
                                                         @RequestBody @Valid ProfilePatchRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.update(userId, req));
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> patchProfile(Authentication authentication,
                                                        @RequestBody @Valid ProfilePatchRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.patch(userId, req));
    }
}
