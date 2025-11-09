package ru.streltsov.microserviceapplication.authservice;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;

    // Заменить на базу!!!!!
    private final Map<String, String> users = Map.of(
            //"user@example.com", "$2a$12$FmR0nz3JNJn38BePB4ITuO8JryQGJNGMHkl85rPNinnv2mG865sxK" 
            //"user@example.com", "$2a$12$zS.ydYh0AtQve.KdRcV8WuR2iju.IXaWjuJgdMJtRqNL6pOL6UwuO" 
            //"user@example.com", "$2a$12$TQWR2lWCtr1M3sAbZKhZqOatKkVP/6kLktC9sFf.in2gC.bP/yXEK" 
            "user@example.com", "$2a$12$/Y81D0Ge3S8x9G8mm.OCs.msoCl8Qj/JdDEejtuzDC/bHDuycoOZ6"
    );

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        System.out.println(" AuthService Создан");
    }

    @Autowired
    private RestTemplate restTemplate;

    @Value("${data.service.url:http://localhost:8084}")
    private String dataServiceUrl;

    public Optional<String> authenticate(String username, String password) {
        System.out.println("AuthService: Запрашиваю пользователя из Data Service: " + username);

        try {
            // Вызываем Data Service: GET /users/username/{username}
            UserData userData = restTemplate.getForObject(
                    dataServiceUrl + "/users/username/" + username,
                    UserData.class
            );

            if (userData != null && BCrypt.checkpw(password, userData.getPasswordHash())) {
                System.out.println("AuthService: Аутентификация успешна для " + username);
                return Optional.of(jwtUtil.generateToken(username, 1L));
            }
        } catch (Exception e) {
            System.err.println("AuthService: Ошибка при запросе к Data Service: " + e.getMessage());
        }

        return Optional.empty();
    }
}
