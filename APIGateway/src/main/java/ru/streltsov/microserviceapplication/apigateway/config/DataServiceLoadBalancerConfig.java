package ru.streltsov.microserviceapplication.apigateway.config;

import java.util.List;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataServiceLoadBalancerConfig {

    @Bean
    public ServiceInstanceListSupplier dataServiceInstanceSupplier() {
        return new StaticServiceInstanceListSupplier(
                "data-service",
                List.of(
                        new DefaultServiceInstance(
                                "data-service-1",
                                "data-service",
                                "localhost",
                                8084,
                                false
                        )
                )
        );
    }
}
