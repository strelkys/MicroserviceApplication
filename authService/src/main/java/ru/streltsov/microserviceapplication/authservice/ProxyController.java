package ru.streltsov.microserviceapplication.authservice;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
public class ProxyController {

    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
/*
    @PostMapping("/api/analyze-defects") // Добавляем новый endpoint
    public ResponseEntity<?> proxyAnalyzeDefects(
            @RequestBody Map<String, Object> payload, // Принимаем JSON-объект, содержащий defectData
            HttpSession session) {

        String token = (String) session.getAttribute("token");
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> request = new HttpEntity<>(payload, headers);

        try {
            // Отправляем на микросервис анализа через API Gateway
            // Путь должен совпадать с тем, что вы настроили в шлюзе (например, /api/analyze-defects)
            // Если ваш микросервис анализа слушает напрямую, используйте его адрес (например, http://localhost:8083/analyze-defects)
            // Но лучше, чтобы всё проходило через API Gateway
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://localhost:8083/api/analyze-defects", // URL API Gateway
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getMessage()));
        }
    }*/
}
