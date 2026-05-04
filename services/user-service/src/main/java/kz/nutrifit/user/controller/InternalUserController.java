package kz.nutrifit.user.controller;

import kz.nutrifit.user.dto.ProfileResponse;
import kz.nutrifit.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.toResponse(profileService.getOrCreate(userId)));
    }
}
