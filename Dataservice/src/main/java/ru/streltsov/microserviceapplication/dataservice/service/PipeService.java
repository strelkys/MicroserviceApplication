/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.streltsov.microserviceapplication.dataservice.model.DrillingPipe;
import ru.streltsov.microserviceapplication.dataservice.repository.PipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 *
 * @author Александр
 */
@Service
public class PipeService {

    @Autowired
    private PipeRepository pipeRepository;

    // Получить все трубы
    public List<DrillingPipe> getAllPipes() {
        return pipeRepository.findAll();
    }

    // Получить трубу по ID
    public DrillingPipe getPipeById(Long id) {
        return pipeRepository.findById(id).orElse(null);
    }

    // Создать новую трубу
    public DrillingPipe createPipe(DrillingPipe pipe) {
        return pipeRepository.save(pipe);
    }

    // Обновить трубу
    public DrillingPipe updatePipe(Long id, DrillingPipe updatedPipe) {
        if (!pipeRepository.existsById(id)) {
            return null;
        }
        updatedPipe.setId(id);
        return pipeRepository.save(updatedPipe);
    }

    // Удалить трубу
    public boolean deletePipe(Long id) {
        if (!pipeRepository.existsById(id)) {
            return false;
        }
        pipeRepository.deleteById(id);
        return true;
    }

    // Генерация тестового набора труб
    public List<DrillingPipe> generateTestPipes(int count) {
        List<DrillingPipe> pipes = new ArrayList<>();
        Random random = new Random();

        String[] types = {
            "НКТ-73",
            "НКТ-89",
            "НКТ-114",
            "API 5CT Grade J55",
            "API 5CT Grade N80",
            "ГОСТ 632-80 Класс А",
            "ГОСТ 632-80 Класс Б"
        };

        for (int i = 0; i < count; i++) {
            String type = types[random.nextInt(types.length)];
            int dataPerSensor = 10 + random.nextInt(50); // От 10 до 59 точек данных на датчик
            DrillingPipe pipe = new DrillingPipe(type, 6, dataPerSensor); // 6 датчиков
            pipes.add(pipe);
        }

        return pipeRepository.saveAll(pipes);
    }
}
