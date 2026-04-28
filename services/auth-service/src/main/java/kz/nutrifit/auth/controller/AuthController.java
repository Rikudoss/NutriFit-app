package kz.nutrifit.auth.controller;

import jakarta.validation.Valid;
import kz.nutrifit.auth.dto.AuthResponse;
import kz.nutrifit.auth.dto.LoginRequest;
import kz.nutrifit.auth.dto.RegisterRequest;
import kz.nutrifit.auth.dto.RegisterResponse;
import kz.nutrifit.auth.dto.ResendCodeRequest;
import kz.nutrifit.auth.dto.VerifyRequest;
import kz.nutrifit.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestBody @Valid VerifyRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendCode(@RequestBody @Valid ResendCodeRequest request) {
        authService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Code resent"));
    }
}
