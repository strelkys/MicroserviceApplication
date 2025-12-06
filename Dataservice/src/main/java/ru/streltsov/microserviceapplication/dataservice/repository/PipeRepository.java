/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.streltsov.microserviceapplication.dataservice.model.DrillingPipe;
/**
 *
 * @author Александр
 */
@Repository
public interface PipeRepository extends JpaRepository<DrillingPipe, Long> {
    // Можно добавить кастомные методы, например:
    // List<Pipe> findByType(String type);
}
