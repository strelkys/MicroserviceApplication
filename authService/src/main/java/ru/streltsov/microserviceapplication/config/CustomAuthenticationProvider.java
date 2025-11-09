package ru.streltsov.microserviceapplication.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import ru.streltsov.microserviceapplication.authservice.AuthService; // Импортируем ваш сервис

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AuthService authService; // Внедряем ваш сервис

    public CustomAuthenticationProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName(); // Получаем имя пользователя из формы (в вашем случае - email)
        String password = authentication.getCredentials().toString(); // Получаем пароль

        // Используем ваш AuthService для проверки
        // authenticate возвращает Optional<String> - токен, если аутентификация успешна
        var token = authService.authenticate(username, password);

        if (token.isPresent()) {
            // Если аутентификация прошла успешно, создаем объект Authentication
            // с правами доступа и возвращаем его
            return new UsernamePasswordAuthenticationToken(
                username, // principal (обычно имя пользователя)
                password, // credentials (обычно пароль, но можно и null, если не нужно хранить дальше)
                AuthorityUtils.createAuthorityList("USER") // authorities
            );
        } else {
            // Если аутентификация не удалась, бросаем исключение
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // Указываем, что этот провайдер поддерживает UsernamePasswordAuthenticationToken
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}