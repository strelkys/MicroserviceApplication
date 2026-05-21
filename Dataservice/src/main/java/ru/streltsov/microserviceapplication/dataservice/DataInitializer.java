/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice;

import ru.streltsov.microserviceapplication.dataservice.model.DrillingPipe;
import ru.streltsov.microserviceapplication.dataservice.model.User;
import ru.streltsov.microserviceapplication.dataservice.service.PipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import ru.streltsov.microserviceapplication.dataservice.repository.UserRepository;

/**
 *
 * @author Александр
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PipeService pipeService;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    
    public void run(String... args) throws Exception {
        // Создаём тестового пользователя если его нет
        String testUsername = "user@example.com";
        if (!userRepository.existsById(testUsername)) {
            String rawPassword = "password123"; // Пароль для тестового пользователя
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            
            User testUser = new User(testUsername, hashedPassword);
            userRepository.save(testUser);
            System.out.println("Создан тестовый пользователь: " + testUsername + " с паролем: " + rawPassword);
        }
        
        /*
        System.out.println("Инициализация данных...");
        for (int i = 0; i < 150; i++) {
            pipeService.createPipe(new DrillingPipe("НКТ-73", 6, 150));
        }
        System.out.println("Данные успешно загружены.");
*/
    }
}
