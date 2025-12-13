/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.apigateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import ru.streltsov.microserviceapplication.apigateway.config.AnalysisServiceLoadBalancerConfig;
import ru.streltsov.microserviceapplication.apigateway.config.AuthServiceLoadBalancerConfig;
import ru.streltsov.microserviceapplication.apigateway.config.DataServiceLoadBalancerConfig;
/**
 *
 * @author Александр
 */

@LoadBalancerClients({
    @LoadBalancerClient(name = "auth-service", configuration = AuthServiceLoadBalancerConfig.class),
    @LoadBalancerClient(name = "data-service", configuration = DataServiceLoadBalancerConfig.class),
    @LoadBalancerClient(name = "analysis-service", configuration = AnalysisServiceLoadBalancerConfig.class)
})
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
