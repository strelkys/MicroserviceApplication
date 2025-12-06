/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice;

import ru.streltsov.microserviceapplication.dataservice.model.DrillingPipe;
import ru.streltsov.microserviceapplication.dataservice.service.PipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.streltsov.microserviceapplication.dataservice.model.DrillingPipe; // или Pipe
import ru.streltsov.microserviceapplication.dataservice.service.PipeService;

/**
 *
 * @author Александр
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PipeService pipeService;

    @Override
    
    public void run(String... args) throws Exception {
        /*
        System.out.println("Инициализация данных...");
        for (int i = 0; i < 150; i++) {
            pipeService.createPipe(new DrillingPipe("НКТ-73", 6, 150));
        }
        System.out.println("Данные успешно загружены.");
*/
    }
}
