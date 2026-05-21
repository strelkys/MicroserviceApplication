/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.streltsov.microserviceapplication.dataservice.model.DrillingPipe;
import ru.streltsov.microserviceapplication.dataservice.service.PipeService;
import ru.streltsov.microserviceapplication.dataservice.service.AnalysisResultService;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Александр
 */

@RestController
@RequestMapping("/pipes")
public class PipeController {

    @Autowired
    private PipeService pipeService;

    @Autowired
    private AnalysisResultService analysisResultService;

    // GET /pipes — получить все трубы
    @GetMapping
    public ResponseEntity<List<DrillingPipe>> getAllPipes() {
        return ResponseEntity.ok(pipeService.getAllPipes());
    }

    // GET /pipes/{id} — получить трубу по ID
    @GetMapping("/{id}")
    public ResponseEntity<DrillingPipe> getPipeById(@PathVariable Long id) {
        DrillingPipe pipe = pipeService.getPipeById(id);
        if (pipe == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pipe);
    }

    // POST /pipes — создать новую трубу
    @PostMapping
    public ResponseEntity<DrillingPipe> createPipe(@RequestBody DrillingPipe pipe) {
        DrillingPipe savedPipe = pipeService.createPipe(pipe);
        return ResponseEntity.ok(savedPipe);
    }

    // PUT /pipes/{id} — обновить трубу
    @PutMapping("/{id}")
    public ResponseEntity<DrillingPipe> updatePipe(@PathVariable Long id, @RequestBody DrillingPipe updatedPipe) {
        DrillingPipe pipe = pipeService.updatePipe(id, updatedPipe);
        if (pipe == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pipe);
    }

    // DELETE /pipes/{id} — удалить трубу
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deletePipe(@PathVariable Long id) {
        boolean deleted = pipeService.deletePipe(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(true);
    }

    // POST /pipes/generate?count=10 — сгенерировать тестовые трубы
    @PostMapping("/generate")
    public ResponseEntity<List<DrillingPipe>> generateTestPipes(@RequestParam(defaultValue = "5") int count) {
        List<DrillingPipe> generated = pipeService.generateTestPipes(count);
        return ResponseEntity.ok(generated);
    }

// GET /pipes/last100 — получить последние 100 труб
    @GetMapping("/last100")
    public ResponseEntity<List<DrillingPipe>> getLast100Pipes() {
        List<DrillingPipe> allPipes = pipeService.getAllPipes();
        int size = allPipes.size();
        int start = Math.max(0, size - 100);
        List<DrillingPipe> last100 = allPipes.subList(start, size);
        return ResponseEntity.ok(last100);
    }
    
    /**
     * GET /pipes/last-analyzed - Получить номер последней проанализированной трубы
     * Возвращает -1 если записей нет или таблица не существует
     */
    @GetMapping("/last-analyzed")
    public ResponseEntity<Map<String, Long>> getLastAnalyzedPipe() {
        Long lastPipeNumber = analysisResultService.getLastAnalyzedPipeNumber();
        return ResponseEntity.ok(Map.of("pipeNumber", lastPipeNumber));
    }
    
    /**
     * POST /pipes/save-result - Сохранить результат анализа трубы
     * @param request запрос с pipeId, predictedClass, operatorDecision
     * @return результат операции сохранения
     */
    @PostMapping("/save-result")
    public ResponseEntity<Map<String, Object>> saveAnalysisResult(@RequestBody Map<String, Object> request) {
        try {
            Long pipeId = Long.valueOf(request.get("pipeId").toString());
            Integer predictedClass = Integer.valueOf(request.get("predictedClass").toString());
            String operatorDecision = request.get("operatorDecision").toString();
            
            boolean success = analysisResultService.saveAnalysisResult(pipeId, predictedClass, operatorDecision);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Результат успешно сохранен"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Ошибка при сохранении результата"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка: " + e.getMessage()
            ));
        }
    }
}
