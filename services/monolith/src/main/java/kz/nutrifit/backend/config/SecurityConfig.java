package kz.nutrifit.backend.config;

import kz.nutrifit.backend.security.XUserIdAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Монолит после Шага C.2: больше НЕ парсит JWT.
 *
 * Gateway валидирует подпись JWT и пробрасывает X-User-Id.
 * Монолит читает заголовок через XUserIdAuthenticationFilter и доверяет ему.
 *
 * Поэтому здесь нет:
 *  - DaoAuthenticationProvider / AuthenticationManager (login/register больше нет в монолите)
 *  - PasswordEncoder (пароли живут только в auth-service)
 *  - UserDetailsService (никто не загружает юзера по email)
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final XUserIdAuthenticationFilter xUserIdAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(XUserIdAuthenticationFilter xUserIdAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.xUserIdAuthenticationFilter = xUserIdAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(xUserIdAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
