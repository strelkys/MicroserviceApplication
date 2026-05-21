/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.streltsov.microserviceapplication.ui.model.InspectionData;

import java.util.concurrent.atomic.AtomicReference;
/**
 *
 * @author Александр
 */
@RestController
@RequestMapping("/api")
public class InspectionController {

    // Для демонстрации используем атомарную ссылку. В реальном проекте это будет сервис с базой данных или очередью.
    private final AtomicReference<InspectionData> latestInspectionData = new AtomicReference<>();

    // GET /api/inspection-data — Получить последние данные для отображения
    @GetMapping("/inspection-data")
    public ResponseEntity<InspectionData> getInspectionData() {
        InspectionData data = latestInspectionData.get();
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    // POST /api/confirm-inspection — Подтвердить или отменить результат
    @PostMapping("/confirm-inspection")
    public ResponseEntity<String> confirmInspection(@RequestBody InspectionData request) {
        // Здесь вы можете сохранить результат в базу данных или отправить в другую систему
        System.out.println("Подтверждение для трубы ID: " + request.getPipeId() + ", статус: " + request.getStatus());

        // Очищаем последние данные, чтобы страница перешла в состояние "ожидания"
        latestInspectionData.set(null);

        return ResponseEntity.ok("OK");
    }

    // Метод для установки данных (вызывается из другого сервиса, например, из analysis-service)
    // Этот метод можно вызывать через REST API или через внутренний вызов
    public void setInspectionData(InspectionData data) {
        latestInspectionData.set(data);
    }
}