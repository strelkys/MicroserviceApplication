package ru.streltsov.microserviceapplication.thickservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель результата анализа трубы
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private Long pipeId;
    private String classification; // "Годно" или "Брак"
    private String heatmapImagePath; // Путь к изображению тепловой карты
    private double averageValue;
    private Integer predictionClass; // 0 - годно, 1 - брак (предсказание нейросети)
}
