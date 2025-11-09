/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.authservice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("Запрос к AuthService: " + request.getUsername());
        Optional<String> token = authService.authenticate(request.getUsername(), request.getPassword());
        if (token.isPresent()) {
            return ResponseEntity.ok(new JwtResponse(token.get()));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}