package kz.nutrifit.auth.service;

import kz.nutrifit.auth.dto.AuthResponse;
import kz.nutrifit.auth.dto.LoginRequest;
import kz.nutrifit.auth.dto.RegisterRequest;
import kz.nutrifit.auth.dto.RegisterResponse;
import kz.nutrifit.auth.dto.VerifyRequest;
import kz.nutrifit.auth.entity.User;
import kz.nutrifit.auth.event.UserEventPublisher;
import kz.nutrifit.auth.repository.UserRepository;
import kz.nutrifit.auth.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private static final String STATUS_UNVERIFIED = "UNVERIFIED";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final UserEventPublisher userEventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       EmailVerificationService emailVerificationService,
                       UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailVerificationService = emailVerificationService;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(STATUS_UNVERIFIED)
                .build();
        user = userRepository.save(user);

        emailVerificationService.generateAndSendCode(user);
        userEventPublisher.publishUserRegistered(user.getId(), user.getEmail());

        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                "Registration successful. Verification code sent to your email.");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (STATUS_UNVERIFIED.equals(user.getStatus())) {
            throw new IllegalStateException("Email not verified. Please check your inbox.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail());
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean ok = emailVerificationService.verifyCode(user.getId(), request.getCode());
        if (!ok) {
            throw new IllegalArgumentException("Invalid or expired code");
        }

        user.setStatus(STATUS_ACTIVE);
        user.setVerifiedAt(Instant.now());
        userRepository.save(user);

        userEventPublisher.publishUserVerified(user.getId(), user.getEmail());

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail());
    }

    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!STATUS_UNVERIFIED.equals(user.getStatus())) {
            throw new IllegalStateException("Email already verified");
        }

        emailVerificationService.resendCode(user);
    }
}
