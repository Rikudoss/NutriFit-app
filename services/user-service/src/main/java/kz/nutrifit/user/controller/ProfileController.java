package kz.nutrifit.user.controller;

import kz.nutrifit.user.dto.ProfilePatchRequest;
import kz.nutrifit.user.dto.ProfileResponse;
import kz.nutrifit.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.toResponse(profileService.getOrCreate(userId)));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication,
                                                         @RequestBody ProfilePatchRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.update(userId, req));
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> patchProfile(Authentication authentication,
                                                        @RequestBody ProfilePatchRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.patch(userId, req));
    }
}
