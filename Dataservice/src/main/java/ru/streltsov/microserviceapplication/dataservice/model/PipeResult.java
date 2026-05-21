package ru.streltsov.microserviceapplication.dataservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Модель результата анализа трубы для базы DefectVKR
 */
@Entity
@Table(name = "result")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pipe_id", nullable = false)
    private Long pipeId;

    @Column(name = "predicted_class", nullable = false)
    private Integer predictedClass; // 0 - годно, 1 - брак (предсказание нейросети)

    @Column(name = "operator_decision", nullable = false)
    private String operatorDecision; // "GOOD" или "DEFECT" (решение оператора)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
