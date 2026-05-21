package ru.streltsov.microserviceapplication.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Контроллер UI для проксирования запросов к микросервисам
 */
@Controller
public class UiController {

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    @Value("${thick.service.url:http://localhost:8080}")
    private String thickServiceUrl;
    
    @Value("${data.service.url:http://localhost:8082}")
    private String dataServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    
    @PostMapping("/api/getNextPipe")
    @ResponseBody
    public ResponseEntity<?> getNextPipe() {
        try {
            // Запрос к ThickService для получения следующей трубы
            ResponseEntity<Map> response = restTemplate.getForEntity(
                thickServiceUrl + "/thick/next-pipe",
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of("error", "ThickService недоступен: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/save-decision - Сохранить решение оператора через DataService
     */
    @PostMapping("/api/save-decision")
    @ResponseBody
    public ResponseEntity<?> saveDecision(@RequestBody Map<String, Object> decision) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(decision, headers);
            
            // Отправляем данные в DataService для сохранения в базу DefectVKR
            ResponseEntity<Map> response = restTemplate.postForEntity(
                dataServiceUrl + "/pipes/save-result",
                request,
                Map.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of(
                "success", false,
                "message", "Ошибка сохранения: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/api/saveDecision")
    @ResponseBody
    public ResponseEntity<?> saveDecisionOld(@RequestBody Map<String, Object> decision) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(decision, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                thickServiceUrl + "/thick/save-decision",
                request,
                Map.class
            );
            
            // После сохранения решения получаем следующую трубу
            return getNextPipe();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of("error", "Ошибка сохранения: " + e.getMessage()));
        }
    }

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/reqvestPage")
    public String reqvestPage() {
        return "reqvestPage";
    }

    @GetMapping("/inspection")
    public String inspection() {
        return "inspection";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(credentials, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                authServiceUrl + "/auth/login",
                request,
                Map.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of("error", "AuthService недоступен: " + e.getMessage()));
        }
    }
    
    /**
     * Прокси для ThickService /thick/start
     */
    @PostMapping("/thick/start")
    @ResponseBody
    public ResponseEntity<?> startWork() {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                thickServiceUrl + "/thick/start",
                null,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of("error", "ThickService недоступен: " + e.getMessage()));
        }
    }
    
    /**
     * Прокси для ThickService /thick/continue
     */
    @PostMapping("/thick/continue")
    @ResponseBody
    public ResponseEntity<?> continueWork(@RequestBody Map<String, Long> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Long>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                thickServiceUrl + "/thick/continue",
                entity,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of("error", "ThickService недоступен: " + e.getMessage()));
        }
    }
    
    /**
     * Прокси для ThickService /thick/heatmap/{pipeId}
     */
    @GetMapping("/thick/heatmap/{pipeId}")
    @ResponseBody
    public byte[] getHeatmap(@PathVariable Long pipeId) {
        try {
            return restTemplate.getForObject(
                thickServiceUrl + "/thick/heatmap/" + pipeId,
                byte[].class
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    @PostMapping("/thick/process/{pipeId}")
    @ResponseBody
    public ResponseEntity<?> processPipe(@PathVariable Long pipeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                thickServiceUrl + "/thick/process/" + pipeId,
                request,
                Map.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body(Map.of("error", "ThickService недоступен: " + e.getMessage()));
        }
    }
}
