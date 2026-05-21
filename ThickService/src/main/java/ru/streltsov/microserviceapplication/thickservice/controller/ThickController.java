package ru.streltsov.microserviceapplication.thickservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.streltsov.microserviceapplication.thickservice.model.AnalysisResult;
import ru.streltsov.microserviceapplication.thickservice.service.ThickDataService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Контроллер для работы с данными толщинометрии
 */
@Slf4j
@RestController
@RequestMapping("/thick")
@CrossOrigin(origins = "*") // Разрешаем CORS для доступа из UI
public class ThickController {

    @Autowired
    private ThickDataService thickDataService;

    /**
     * Получить raw файл для указанной трубы
     * GET /thick/raw/{pipeId}
     */
    @GetMapping("/raw/{pipeId}")
    public ResponseEntity<byte[]> getRawFile(@PathVariable Long pipeId) {
        try {
            String rawFilePath = thickDataService.exportThickToRaw(pipeId);
            
            if (rawFilePath == null || !new File(rawFilePath).exists()) {
                return ResponseEntity.notFound().build();
            }
            
            File file = new File(rawFilePath);
            byte[] content = Files.readAllBytes(file.toPath());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/octet-stream")
                    .header("Content-Disposition", "attachment; filename=\"thick_" + pipeId + ".raw\"")
                    .body(content);
        } catch (Exception e) {
            log.error("Error getting raw file for pipe ID: {}", pipeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Анализировать трубу и вернуть результат классификации
     * POST /thick/analyze/{pipeId}
     */
    @PostMapping("/analyze/{pipeId}")
    public ResponseEntity<AnalysisResult> analyzePipe(@PathVariable Long pipeId) {
        try {
            // Экспортируем данные в raw файл
            String rawFilePath = thickDataService.exportThickToRaw(pipeId);
            
            if (rawFilePath == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Выполняем анализ (пока временная реализация)
            AnalysisResult result = thickDataService.analyzeData(rawFilePath, pipeId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error analyzing pipe ID: {}", pipeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получить тепловую карту для указанной трубы
     * GET /thick/heatmap/{pipeId}
     */
    @GetMapping("/heatmap/{pipeId}")
    public ResponseEntity<byte[]> getHeatmap(@PathVariable Long pipeId) {
        try {
            // Сначала экспортируем данные если их нет
            String rawFilePath = thickDataService.exportThickToRaw(pipeId);
            
            if (rawFilePath == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Генерируем или получаем существующую тепловую карту
            String heatmapPath = thickDataService.generateHeatmap(rawFilePath, pipeId);
            
            File heatmapFile = new File(heatmapPath);
            if (!heatmapFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] content = Files.readAllBytes(heatmapFile.toPath());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(content);
        } catch (Exception e) {
            log.error("Error getting heatmap for pipe ID: {}", pipeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Комбинированный эндпоинт: экспорт + анализ + получение результата для UI
     * POST /thick/process/{pipeId}
     */
    @PostMapping("/process/{pipeId}")
    public ResponseEntity<AnalysisResult> processPipe(@PathVariable Long pipeId) {
        try {
            // Экспортируем данные в raw файл из базы Derby
            String rawFilePath = thickDataService.exportThickToRaw(pipeId);
            
            if (rawFilePath == null) {
                log.warn("No thick data found for pipe ID: {}", pipeId);
                return ResponseEntity.notFound().build();
            }
            
            // Отправляем файл в Python сервис для анализа и получения результата
            AnalysisResult result = thickDataService.analyzeData(rawFilePath, pipeId);
            
            log.info("Processed pipe ID {}: status={}", pipeId, result.getClassification());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing pipe ID: {}", pipeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
