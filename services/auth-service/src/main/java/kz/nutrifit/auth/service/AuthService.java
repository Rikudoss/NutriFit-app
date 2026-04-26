package kz.nutrifit.auth.service;

import kz.nutrifit.auth.dto.AuthResponse;
import kz.nutrifit.auth.dto.LoginRequest;
import kz.nutrifit.auth.dto.RegisterRequest;
import kz.nutrifit.auth.entity.User;
import kz.nutrifit.auth.repository.UserRepository;
import kz.nutrifit.auth.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        // Шаг A: status=ACTIVE по умолчанию (см. User.@Builder.Default).
        // Шаг B заменит на UNVERIFIED + email-верификацию.
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        // TODO Фаза 3: вместо локального создания Profile — публикация события user.registered в Kafka,
        // user-service создаст профиль по событию.

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
