/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.streltsov.microserviceapplication.dataservice.model.User;

import java.util.Optional;

/**
 *
 * @author Александр
 */
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
