/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.config;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.streltsov.microserviceapplication.authservice.AuthService;

import java.util.Optional;
/**
 *
 * @author StreltsovAE
 */
@Service
public class CustomUserDetailsService  implements UserDetailsService {
    
    private final AuthService authService;

    public CustomUserDetailsService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Здесь мы используем ваш AuthService для проверки пользователя
        // Предполагаем, что authenticate возвращает токен, если пользователь валиден
        // Для UserDetailsService нам нужно вернуть UserDetails
        // В вашем случае, так как вы используете JWT, мы можем создать "фиктивного" пользователя
        // с любым паролем, так как реальная проверка будет происходить позже при генерации JWT
        
        // Проверим, существует ли пользователь в вашем "хранилище"
        // Это немного хак, но работает для демонстрации
        // В реальном приложении лучше хранить UserDetails или сущность User
        Optional<String> token = authService.authenticate(username, "password"); // dummy password, real check is in JWT filter
        System.out.println("CustomUserDetailsService token: " + token);
        if (token.isPresent()) {
            System.out.println("CustomUserDetailsService token.isPresent()? : true" );
            // Если пользователь существует, создаем UserDetails
            // В целях демонстрации используем username как пароль (он не будет проверяться стандартным фильтром)
            // Потому что мы заменим фильтр аутентификации своим
            return User.withUsername(username)
                       .password("{noop}" + username) // {noop} отключает кодирование пароля для этого примера
                       .authorities(AuthorityUtils.createAuthorityList("USER"))
                       .build();
        } else {
            System.out.println("CustomUserDetailsService token.isPresent()? : false" );
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}