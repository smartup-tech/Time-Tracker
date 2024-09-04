package ru.smartup.timetracker.core;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.service.UserService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DBAuthenticationProvider implements AuthenticationProvider {
    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Optional<UsernamePasswordAuthenticationToken> usernamePasswordAuthenticationToken
                = getUsernamePasswordAuthenticationToken(authentication.getName(), authentication.getCredentials().toString());
        usernamePasswordAuthenticationToken.orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));
        return usernamePasswordAuthenticationToken.get();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Получить авторизационный токен пользователя.
     * При создании токена не устанавливаем роли в сессионные данные, так как в этом месте они не видны регистру сессий.
     * Роли будут установлены при первом запросе пользователя
     *
     * @param email    почта
     * @param password пароль
     * @return UsernamePasswordAuthenticationToken токен
     */
    private Optional<UsernamePasswordAuthenticationToken> getUsernamePasswordAuthenticationToken(String email, String password) {
        Optional<User> userOptional = userService.getNotArchivedUserByEmail(email);
        userOptional.orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));
        User user = userOptional.get();
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return Optional.of(new UsernamePasswordAuthenticationToken(new SessionUserPrincipal(user.getId(),
                    user.getEmail()), null, null));
        }
        return Optional.empty();
    }
}
