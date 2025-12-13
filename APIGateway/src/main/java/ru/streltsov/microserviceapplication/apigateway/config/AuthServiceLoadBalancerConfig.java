package ru.streltsov.microserviceapplication.apigateway.config;

import java.util.List;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthServiceLoadBalancerConfig {

    @Bean
    public ServiceInstanceListSupplier authServiceInstanceSupplier() {
        return new StaticServiceInstanceListSupplier(
                "auth-service",
                List.of(
                        new DefaultServiceInstance(
                                "auth-service-1",
                                "auth-service",
                                "localhost",
                                8081,
                                false
                        )
                )
        );
    }
}
