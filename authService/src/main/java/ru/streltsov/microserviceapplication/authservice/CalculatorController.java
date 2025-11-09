package ru.streltsov.microserviceapplication.authservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalculatorController {

    @GetMapping("/calculator")
    public String calculatorPage() {
        return "calculator";
    }
}