package kz.nutrifit.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Читает заголовок X-User-Id, который Gateway пробрасывает после успешной валидации JWT.
 *
 * Контракт:
 *  - Gateway уже проверил подпись JWT (общий JWT_SECRET) и expiration.
 *  - Если запрос дошёл сюда с X-User-Id — Gateway ему доверил, монолит доверяет тоже.
 *  - Если запрос дошёл без X-User-Id — это публичный путь (Gateway whitelist) либо прямой
 *    запрос в обход Gateway. Spring Security дальше решит, нужна ли аутентификация
 *    (anyRequest().authenticated() даст 401 для приватных эндпоинтов).
 *
 * Принципиально: principal = Long userId, не User entity.
 *  - Нет похода в БД на каждый запрос (filter не делает findById).
 *  - Контроллеры читают: Long userId = (Long) authentication.getPrincipal();
 *  - Если контроллеру нужны поля User — он явно делает userRepository.findById(userId)
 *    или getReferenceById(userId) для FK.
 */
@Component
@Slf4j
public class XUserIdAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String headerValue = request.getHeader(USER_ID_HEADER);
        if (headerValue == null || headerValue.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = Long.parseLong(headerValue);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (NumberFormatException ex) {
            log.warn("Invalid X-User-Id header value: '{}' — request continues unauthenticated", headerValue);
        }

        filterChain.doFilter(request, response);
    }
}
